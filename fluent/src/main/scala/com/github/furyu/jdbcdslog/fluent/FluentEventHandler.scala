package com.github.furyu.jdbcdslog.fluent

import org.jdbcdslog.plugin.EventHandler
import java.util
import org.fluentd.logger.FluentLogger
import com.github.stephentu.scalasqlparser._
import scala.util.DynamicVariable
import annotation.tailrec
import java.sql.{Statement, PreparedStatement}

class FluentEventHandler extends EventHandler {

  import DefaultWrites._

  val currentContext: DynamicVariable[Option[Context]] = new DynamicVariable(None)

  val logger = FluentLogger.getLogger("debug.test")
  val parser = new SQLParser()
  val resolver = new Resolver {}
  val jdbcUrlPattern = """jdbc:mysql://([^\:]+):(\d+)/(.+)""".r
  val schemas = new scala.collection.mutable.HashMap[String, MySQLSchema]
    with scala.collection.mutable.SynchronizedMap[String, MySQLSchema]

  private def schemaFor(prepStmt: Statement) = {
    val url = prepStmt.getConnection.getMetaData.getURL
    schemas.getOrElseUpdate(
      url,
      url match {
        case jdbcUrlPattern(host, port, db) =>
          new MySQLSchema(host, port.toInt, db, new util.Properties())
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
      val schema = schemaFor(prepStmt)
      resolver.resolve(stmt2, schema.loadSchema())
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
