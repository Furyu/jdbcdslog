package com.github.furyu.jdbcdslog.fluent

import org.jdbcdslog.plugin.EventHandler
import java.util
import org.fluentd.logger.FluentLogger
import com.github.stephentu.scalasqlparser._
import scala.util.DynamicVariable
import java.sql.Statement
import org.slf4j.LoggerFactory

/**
 * Pluggable java.sql.Connection provider used by FluentEventHandler.
 *
 * FluentEventHandler uses an instance of ConnectionProvider while it looks up for
 * table/column definitions to resolve relation names (aliases) in SQL statements.
 */
trait ConnectionProvider {
  /**
   * Execute a block with a java.sql.Connection as an argument.
   *
   * @param url JDBC url e.g. jdbc:mysql://host/database
   * @param block the block executed with an provided java.sql.Connection as an argument
   * @tparam T the return type of block and this function
   * @return the value the block returned
   */
  def withConnection[T](url: String)(block: java.sql.Connection => T): T
}

object ConnectionProvider {
  /**
   * The underlying ConnectionProvider instance used to provided connections.
   * You should set this before FluentEventHandler#statement is called for first time.
   */
  var provider: Option[ConnectionProvider] = None

  /**
   * Execute a block with a java.sql.Connection as an argument.
   *
   * @param url JDBC url e.g. jdbc:mysql://host/database
   * @param block the block executed with an provided java.sql.Connection as an argument
   * @tparam T the return type of block and this function
   * @return the value the block returned
   */
  def withConnection[T](url: String)(block: java.sql.Connection => T): Option[T] =
    provider.map(_.withConnection(url)(block))
}

/**
 * FluentEventHandler is a jdbcdslog plugin to record every SQL statements executed through JDBC to Fluentd.
 *
 * It works as FluentEventHandler#statement is invoked when jdbcdslog detects invocations of
 * - java.sql.Statement
 * - java.sql.CallableStatement
 * - java.sql.PreparedStatement
 *
 *  FluentEventHandler analyzes or resolves relations, fields in statements and normalize every fields in
 *    `relation name (or alias)`.`column name`
 *  to
 *    `table name`.`column name`
 *  for later use e.g. log analysis.
 */
class FluentEventHandler extends EventHandler {

  import DefaultWrites._

  val currentContext: DynamicVariable[Option[Context]] = new DynamicVariable(None)

  val log = LoggerFactory.getLogger(classOf[FluentEventHandler])
  val logger = FluentLogger.getLogger("debug.test")
  val parser = new SQLParser()
  val resolver = new Resolver {}
  val jdbcUrlPattern = """jdbc:mysql://([^\:]+):(\d+)/(.+)""".r
  val schemas = new scala.collection.mutable.HashMap[String, Definitions]
    with scala.collection.mutable.SynchronizedMap[String, Definitions]

  private def schemaFor(prepStmt: Statement): Definitions = {
    val conn = prepStmt.getConnection
    val url = conn.getMetaData.getURL
    schemas.getOrElseUpdate(
      url,
      url match {
        case jdbcUrlPattern(host, port, db) =>
            ConnectionProvider.withConnection(url) { conn =>
              new MySQLSchema(conn, db).loadSchema()
            }.getOrElse {
              throw new RuntimeException("ConnectionProvider must be registered to use FleuntEventHandler")
            }
        // TODO Support more databases
        case u =>
          throw new RuntimeException("Unexpected format of JDBC url: " + u)
      }
    )
  }

  def withContext[T](c: Context)(b: => T): T =
    currentContext.withValue(Some(c))(b)

  def statement(prepStmt: Statement, sql: String, parameters: util.Map[_, _], time: Long) {
    val label = "default"
    val data = new util.HashMap[String, AnyRef]()
    val db = new util.HashMap[String, AnyRef]()
    data.put("db", db)
    val stmt = parser.parse(sql).map { stmt2 =>
      log.debug(stmt2.toString)
      try {
        val schema = schemaFor(prepStmt)
        resolver.resolve(stmt2, schema)
      } catch {
        case e: ResolutionException =>
          log.warn("Unexpectedly got an ResolutionException while resolving a SQL statement. schemas=" + schemas, e)
          stmt2
      }
    }.map(implicitly[JavaMapWrites[Stmt]].writes).map { stmt =>
      import collection.JavaConverters._
      for ((k,v) <- stmt.asInstanceOf[util.Map[String, AnyRef]].asScala) {
        db.put(k, v)
      }
    }
    data.put("timing", time.asInstanceOf[AnyRef])
    currentContext.value.foreach { context =>
      context.toMap.foreach {
        case (k, v: Map[_, _]) =>
          data.put(k, toJavaMap(v))
        case (k, v: AnyRef) =>
          data.put(k, v)
      }
    }
    println("data=" + data)
    logger.log(label, data)
  }

  def toJavaMap(m: Map[_, _]): util.Map[String, AnyRef] = {
    import scala.collection.JavaConverters._
    m.map {
      case (k, v: Map[_, _]) =>
        k.toString -> toJavaMap(v)
      case (k, v: AnyRef) =>
        k.toString -> v
    }.asJava
  }
}
