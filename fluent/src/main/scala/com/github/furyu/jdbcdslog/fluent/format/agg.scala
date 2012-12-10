package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SeqWrites, SqlExprWrites, JavaMapWrites}

object agg {

  def agg(name: String, options: (String, Any)*) = {
    expression(name, obj(options:_*))
  }

  implicit val aggCallWrites = writes[AggCall] {
    case AggCall(name, args, _) =>
      agg(name, "args" -> args)
  }

  implicit val avg = writes[Avg] {
    case Avg(expr, distinct, _) =>
      agg("$avg", "expr" -> expr, "distinct" -> distinct)
  }

  implicit val countWrites = writes[CountExpr] {
    case CountExpr(expr, distinct, _) =>
      agg("$count", "expr" -> expr, "distinct" -> distinct)
  }

  implicit val countStarWrites = writes[CountStar] {
    case CountStar(_) =>
      agg("$count", "expr" -> "*")
  }

  implicit val groupConcatWrites = writes[GroupConcat] {
    case GroupConcat(expr, sep, _) =>
      agg("$groupConcat", "expr" -> expr, "sep" -> sep)
  }

  implicit val maxWrites = writes[Max] {
    case Max(expr, _) =>
      agg("$max", "expr" -> expr)
  }

  implicit val minWrites = writes[Min] {
    case Min(expr, _) =>
      agg("$min", "expr" -> expr)
  }

  implicit val sumWrites = writes[Sum] {
    case Sum(expr, distinct, _) =>
      agg("$sum", "expr" -> expr, "distinct" -> distinct)
  }

}
