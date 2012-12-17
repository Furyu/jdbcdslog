package com.github.furyu.jdbcdslog.fluent

import org.jdbcdslog.plugin.EventHandler
import java.util
import org.fluentd.logger.FluentLogger
import com.github.stephentu.scalasqlparser._
import scala.util.DynamicVariable
import java.sql.Statement
import org.slf4j.LoggerFactory

trait ConnectionProvider {
  def withConnection[T](url: String)(block: java.sql.Connection => T): T
}

object ConnectionProvider {
  var provider: Option[ConnectionProvider] = None
  def withConnection[T](url: String)(block: java.sql.Connection => T): Option[T] = provider.map(_.withConnection(url)(block))
}

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
