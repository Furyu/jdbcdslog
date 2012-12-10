package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}
import java.util

object binop {

  def binOp(name: String, lhs: SqlExpr, rhs: SqlExpr, options: (String, Any)*) = {
    val data = new java.util.HashMap[String, Any]()
    data.put("name", name)
    data.put("lhs", SqlExprWrites.writes(lhs))
    data.put("rhs", SqlExprWrites.writes(rhs))
    options.foreach { case (key, value) =>
      data.put(key, value)
    }
    data
  }

  implicit val eqWrites = writes[Eq] { a =>
    val Eq(lhs, rhs, _) = a
    binOp("eq", lhs, rhs)
  }

  implicit val NeqWrites = writes[Neq] { neq =>
    val Neq(lhs, rhs, _) = neq
    binOp("neq", lhs, rhs)
  }

  implicit val plusWrites = writes[Plus] { p =>
    val Plus(lhs, rhs, _) = p
    binOp("plus", lhs, rhs)
  }

  implicit val minusWrites = writes[Minus] { p =>
    val Minus(lhs, rhs, _) = p
    binOp("minus", lhs, rhs)
  }

  implicit val addExprWrites = writes[AddExpr] {
    case p: Plus =>
      plusWrites.writes(p)
    case m: Minus =>
      minusWrites.writes(m)
  }

  implicit val andWrites = writes[And] { a =>
    val And(lhs, rhs, _) = a
    binOp("and", lhs, rhs)
  }

  implicit val orWrites = writes[Or] { o =>
    val Or(lhs, rhs, _) = o
    binOp("or", lhs, rhs)
  }

  implicit val likeWrites = writes[Like] { l =>
    val Like(lhs, rhs, negate, _) = l
    binOp("like", lhs, rhs, "negate" -> negate)
  }

  implicit val geWrites = writes[Ge] { ge =>
    val Ge(lhs, rhs, _) = ge
    binOp("ge", lhs, rhs)
  }

  implicit val gtWrites = writes[Gt] { gt =>
    val Gt(lhs, rhs, _) = gt
    binOp("gt", lhs, rhs)
  }

  implicit val leWrites = writes[Le] { le =>
    val Le(lhs, rhs, _) = le
    binOp("le", lhs, rhs)
  }

  implicit val ltWrites = writes[Lt] { lt =>
    val Lt(lhs, rhs, _) = lt
    binOp("lt", lhs, rhs)
  }

  implicit val ineqLikeWrites = writes[InequalityLike] {
    case ge: Ge =>
      geWrites.writes(ge)
    case gt: Gt =>
      gtWrites.writes(gt)
    case le: Le =>
      leWrites.writes(le)
    case lt: Lt =>
      ltWrites.writes(lt)
  }

  implicit val mulWrites = writes[Mult] {
    case Mult(lhs, rhs, _) =>
      binOp("mul", lhs, rhs)
  }

  implicit val divWrites = writes[Div] {
    case Div(lhs, rhs, _) =>
      binOp("div", lhs, rhs)
  }
}