package jp.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import java.util
import jp.furyu.jdbcdslog.fluent.DefaultWrites.{SymbolWrites, SeqWrites, JavaMapWrites, SqlExprWrites}
import jp.furyu.jdbcdslog.fluent.DefaultWrites
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

  implicit val caseWhenExprWrites: JavaMapWrites[CaseWhenExpr] = writes[CaseWhenExpr] {
    case CaseWhenExpr(cases, expr, _) =>
      val data = new util.HashMap[String, Fluent]()
      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      expr.foreach { e =>
        data.put("expr", SqlExprWrites.writes(e))
      }
      data
  }

  implicit val existsWrites: JavaMapWrites[Exists] = writes[Exists] {
    case Exists(select, _) =>
      expression("exists", select)
  }

  implicit val inWrites: JavaMapWrites[In] = writes[In] {
    case In(elem, set, negate, _) =>
      val in = expression("in", obj("elem" -> elem, "set" -> set))
      if (negate)
        expression("not", in)
    else
        in
  }

  implicit val unaryPlusWrites: JavaMapWrites[UnaryPlus] = writes[UnaryPlus] {
    case UnaryPlus(ex, _) =>
      expression("unaryPlus", ex)
  }

  implicit val unaryMinusWrites: JavaMapWrites[UnaryMinus] = writes[UnaryMinus] {
    case UnaryMinus(ex, _) =>
      expression("unaryMinus", ex)
  }

  implicit val notWrites: JavaMapWrites[Not] = writes[Not] {
    case Not(ex, _) =>
      expression("not", ex)
  }

  implicit val unopWrites: JavaMapWrites[Unop] = writes[Unop] {
    case p: UnaryPlus =>
      implicitly[JavaMapWrites[UnaryPlus]].writes(p)
    case m: UnaryMinus =>
      implicitly[JavaMapWrites[UnaryMinus]].writes(m)
    case n: Not =>
      implicitly[JavaMapWrites[Not]].writes(n)
  }

  implicit val postfixUnopWrites: JavaMapWrites[PostfixUnop] = writes[PostfixUnop] {
    case p =>
      expression(p.opStr, p.expr)
  }

  implicit val subselectWrites = writes[Subselect] {
    case Subselect(subquery, _) =>
      expression("subquery", subquery)
  }

}
