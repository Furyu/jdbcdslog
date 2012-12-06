package com.github.furyu.jdbcdslog.fluent

import org.jdbcdslog.plugin.EventHandler
import java.util
import org.fluentd.logger.FluentLogger
import com.github.stephentu.scalasqlparser._
import scala.util.DynamicVariable

trait Context {
  def toMap: Map[String, AnyRef]
}

object SampleContext extends Context {
  def toMap = Map(
    "a" -> 1.asInstanceOf[AnyRef],
    "b" -> Map(
      "c" -> 2.asInstanceOf[AnyRef]
    )
  )
}

class FluentEventHandler extends EventHandler {
  val currentContext: DynamicVariable[Option[Context]] = new DynamicVariable(None)

  val logger = FluentLogger.getLogger("debug.test")
  val parser = new SQLParser()

  def preparedStatement(sql: String, parameters: util.Map[_, _], time: Long) {
    currentContext.value = Some(SampleContext)
    val label = "default"
    val data = new util.HashMap[String, AnyRef]()
    val db = new util.HashMap[String, AnyRef]()
    data.put("db", db)
    parser.parse(sql).map { stmt =>
      stmt match {
        case InsertStmt(tableName, insRow, _) =>
          db.put("command", "insert")
          db.put("table", tableName)
          db.put("timestamp", new java.util.Date().getTime.asInstanceOf[AnyRef])
          val params = new util.HashMap[String, AnyRef]()
          db.put("params", params)
          insRow match {
            case com.github.stephentu.scalasqlparser.Set(assigns, _) =>
              assigns.foreach { assign =>
                params.put(assign.lhs.sql, assign.rhs.sql)
              }
            case com.github.stephentu.scalasqlparser.Values(values, _) =>
              values.foreach { value =>
                params.put("colNameFor" + value, value.sql)
              }
          }
      }
    }
    currentContext.value.foreach { context =>
      context.toMap.foreach {
        case (k, v: Map[_, _]) =>
          data.put(k, toJavaMap(v))
        case (k, v: AnyRef) =>
          data.put(k, v)
      }
    }
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
