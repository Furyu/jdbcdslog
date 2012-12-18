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
   * You must set this before FluentEventHandler#statement is called for first time.
   *
   * We don't like any `var`, but we need it HERE.
   *
   * We make this not `val` but `var` to allow substituting providers in freedom for cases of
   * integrating FluentEventHandler with various libraries or frameworks.
   * While integration, please write your own mechanism to configure ConnectionProvider in your preferred
   * configuration files, on top of this var.
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
 *
 * @param props the keys `jdbcdslog.fluent.tag` and `jdbcdslog.fluent.label` contains tag and label added
 *              to log data sent with fluent-java-logger.
 */
class FluentEventHandler(props: java.util.Properties) extends EventHandler {

  import DefaultWrites._

  /**
   * We need this dynamic variable to pass context-specific data included in the data sent to Fluentd,
   * from our application to FluentEventHandler.
   *
   * Remember that dynamic variables are very similar to thread-locals, thus the same consideration is needed
   * to use them properly.
   * For instance, you should not switch between threads after you set a value for the dynamic variable until
   * any SQL statement you intended to record is executed.
   */
  val currentContext: DynamicVariable[Option[Context]] = new DynamicVariable(None)

  val slf4jLogger = LoggerFactory.getLogger(classOf[FluentEventHandler])
  val fluentLogger = {

    slf4jLogger.debug("props=" + props)

    val tag = props.get("jdbcdslog.fluent.tag") match {
      case t: String =>
        t
      case _ =>
        "debug"
    }
    val host = props.get("jdbcdslog.fluent.host") match {
      case t: String =>
        t
      case _ =>
        "localhost"
    }
    val port = props.get("jdbcdslog.fluent.port") match {
      case t: String =>
        try {
          t.toInt
        } catch {
          case e: NumberFormatException =>
            slf4jLogger.warn("jdbcdslog.fluent.port(=" + t + ") must be a number but it was not. " +
              "Defaulting to 24224 (Fluentd's default port)")
          24224
        }
      case _ =>
        24224
    }
    FluentLogger.getLogger(tag, host, port)
  }
  val label = props.get("jdbcdslog.fluent.label") match {
    case t: String =>
      t
    case _ =>
      slf4jLogger.info("jdbcdslog.fluent.label is not provided in properties. " +
        "Defaulting to \"test\".")
      "test"
  }
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

  /**
   * Evaluated a block of code with the given context.
   * Wrap any block of code IN THE SAMEwith this method if you want to pass
   * @param c
   * @param b
   * @tparam T
   * @return
   */
  def withContext[T](c: Context)(b: => T): T =
    currentContext.withValue(Some(c))(b)

  def statement(prepStmt: Statement, sql: String, parameters: util.Map[_, _], time: Long) {
    val data = new util.HashMap[String, AnyRef]()
    val db = new util.HashMap[String, AnyRef]()
    data.put("db", db)
    val stmt = parser.parse(sql).map { stmt2 =>
      slf4jLogger.debug(stmt2.toString)
      try {
        val schema = schemaFor(prepStmt)
        resolver.resolve(stmt2, schema)
      } catch {
        case e: ResolutionException =>
          slf4jLogger.warn("Unexpectedly got an ResolutionException while resolving a SQL statement. schemas=" + schemas, e)
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
    slf4jLogger.debug("The data being sent to Fluentd=" + data)
    fluentLogger.log(label, data)
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
