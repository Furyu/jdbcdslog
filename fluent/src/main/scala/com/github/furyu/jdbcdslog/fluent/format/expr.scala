package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import java.util
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SymbolWrites, SeqWrites, JavaMapWrites, SqlExprWrites}
import com.github.furyu.jdbcdslog.fluent.DefaultWrites
import com.github.stephentu.scalasqlparser.CaseExprCase
import com.github.stephentu.scalasqlparser.CaseWhenExpr
import com.github.stephentu.scalasqlparser.FieldIdent
import com.github.stephentu.scalasqlparser.Subselect
import com.github.stephentu.scalasqlparser.In
import com.github.stephentu.scalasqlparser.CaseExpr
import com.github.stephentu.scalasqlparser.Exists

object expr {

  type Fluent = Any

  implicit object CaseExprCaseWrites extends JavaMapWrites[CaseExprCase] {
    def writes(a: CaseExprCase) = {
      val CaseExprCase(cond, expr, _) = a
      val data = new util.HashMap[String, Fluent]()
      data.put("cond", SqlExprWrites.writes(cond))
      data.put("expr", SqlExprWrites.writes(expr))
      data
    }
  }

  implicit object SeqCaseExprCaseWrites extends SeqWrites[CaseExprCase]

  implicit object CaseExprWrites extends JavaMapWrites[CaseExpr] {
    def writes(a: CaseExpr) = {
      val data = new util.HashMap[String, Fluent]()
      val CaseExpr(expr, cases, default, _) = a
      data.put("expr", SqlExprWrites.writes(expr))
      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      data
    }
  }

  implicit val caseWhenExprWrites = writes[CaseWhenExpr] {
    case CaseWhenExpr(cases, expr, _) =>
      val data = new util.HashMap[String, Fluent]()
      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      expr.foreach { e =>
        data.put("expr", SqlExprWrites.writes(e))
      }
      data
  }

  implicit val existsWrites = writes[Exists] {
    case Exists(select, _) =>
      expression("$exists", select)
  }

  implicit val inWrites = writes[In] {
    case In(elem, set, negate, _) =>
      val in = expression("$in", obj("elem" -> elem, "set" -> set))
      if (negate)
        expression("$not", in)
    else
        in
  }

  implicit val unaryPlusWrites = writes[UnaryPlus] {
    case UnaryPlus(ex, _) =>
      expression("$unaryPlus", ex)
  }

  implicit val unaryMinusWrites = writes[UnaryMinus] {
    case UnaryMinus(ex, _) =>
      expression("$unaryMinus", ex)
  }

  implicit val notWrites = writes[Not] {
    case Not(ex, _) =>
      expression("$not", ex)
  }

  implicit val unopWrites = writes[Unop] {
    case p: UnaryPlus =>
      implicitly[JavaMapWrites[UnaryPlus]].writes(p)
    case m: UnaryMinus =>
      implicitly[JavaMapWrites[UnaryMinus]].writes(m)
    case n: Not =>
      implicitly[JavaMapWrites[Not]].writes(n)
  }

  implicit val subselectWrites = writes[Subselect] {
    case Subselect(subquery, _) =>
      expression("$subquery", subquery)
  }

  implicit object FieldIdentWrites extends JavaMapWrites[FieldIdent] {
    def writes(fi: FieldIdent) = {
      val FieldIdent(qualifier, name, symbol, _) = fi
      val data = new util.HashMap[String,Any]()
      qualifier.foreach { q =>
        data.put("qualifier", q)
      }
      data.put("name", name)
      data.put("symbol", SymbolWrites.writes(symbol))
      data

      expression("$field", data)
    }
  }

}
