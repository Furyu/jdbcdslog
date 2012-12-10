package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SeqWrites, SqlExprWrites, JavaMapWrites}

object agg {

  val seqExprWrites = new SeqWrites[SqlExpr]()

  def consume(data: Any) {
    data match {
      case expr: SqlExpr =>
        SqlExprWrites.writes(expr)
      case seq: Seq[_] =>
        seqExprWrites.writes(seq.asInstanceOf[Seq[SqlExpr]])
      case any: Any =>
        any
    }
  }

  def agg(name: String, options: (String, Any)*) = {
    val data = new java.util.HashMap[String, Any]()
    data.put("name", name)
    options.foreach {
      case (key, any) =>
        data.put(key, consume(any))
    }
    data
  }

  implicit val aggCallWrites = writes[AggCall] {
    case AggCall(name, args, _) =>
      agg(name, "args" -> args)
  }

  implicit val avg = writes[Avg] {
    case Avg(expr, distinct, _) =>
      agg("avg", "expr" -> expr, "distinct" -> distinct)
  }

  implicit val countWrites = writes[CountExpr] {
    case CountExpr(expr, distinct, _) =>
      agg("count", "expr" -> expr, "distinct" -> distinct)
  }

  implicit val countStarWrites = writes[CountStar] {
    case CountStar(_) =>
      agg("count", "expr" -> "*")
  }

  implicit val groupConcatWrites = writes[GroupConcat] {
    case GroupConcat(expr, sep, _) =>
      agg("group_concat", "expr" -> expr, "sep" -> sep)
  }

  implicit val maxWrites = writes[Max] {
    case Max(expr, _) =>
      agg("max", "expr" -> expr)
  }

  implicit val minWrites = writes[Min] {
    case Min(expr, _) =>
      agg("min", "expr" -> expr)
  }

  implicit val sumWrites = writes[Sum] {
    case Sum(expr, distinct, _) =>
      agg("sum", "expr" -> expr, "distinct" -> distinct)
  }

}
