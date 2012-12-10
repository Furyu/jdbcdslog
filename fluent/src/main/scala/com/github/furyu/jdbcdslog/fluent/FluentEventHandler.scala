package com.github.furyu.jdbcdslog.fluent

import org.jdbcdslog.plugin.EventHandler
import java.util
import org.fluentd.logger.FluentLogger
import com.github.stephentu.scalasqlparser._
import scala.util.DynamicVariable
import annotation.tailrec

class FluentEventHandler extends EventHandler {

  import DefaultWrites._

  val currentContext: DynamicVariable[Option[Context]] = new DynamicVariable(None)

  val logger = FluentLogger.getLogger("debug.test")
  val parser = new SQLParser()

  def withContext[T](c: Context)(b: => T): T =
    currentContext.withValue(Some(c))(b)

  def preparedStatement(sql: String, parameters: util.Map[_, _], time: Long) {
    val label = "default"
    val data = new util.HashMap[String, AnyRef]()
    val db = new util.HashMap[String, AnyRef]()
    data.put("db", db)
    val stmt = parser.parse(sql).map(implicitly[JavaMapWrites[Stmt]].writes).map { stmt =>
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
