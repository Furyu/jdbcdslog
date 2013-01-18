package jp.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import jp.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}
import java.util

object binop {

  def binOp(name: String, lhs: SqlExpr, rhs: SqlExpr) = {
    expression(name, obj("lhs" -> lhs, "rhs" -> rhs))
  }

  import expr._

  implicit val eqWrites = writes[Eq] { a =>
    val Eq(lhs, rhs, _) = a
//    binOp("eq", lhs, rhs)
    val key = lhs match {
      case f: FieldIdent =>
        import field_ident.str
        implicitly[JavaMapWrites[FieldIdent]].writes(f).toString
      case _ =>
        lhs.sql
    }
    obj(key -> rhs)
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

  implicit val likeWrites = writes[Like] {
    case Like(lhs, rhs, negate, _) =>
      val like = binOp("like", lhs, rhs)
      if (negate)
        expression("not", like)
      else
        like
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
