package jp.furyu.jdbcdslog.fluent.format

import jp.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}
import com.github.stephentu.scalasqlparser.{Assign, SqlExpr, FieldIdent, InsRow}
import java.util

object insrow {

  def assignsToJavaMap(assigns: Seq[Assign]) = {
    import expr._
    import field_ident.str
    val fieldIdentWrites = implicitly[JavaMapWrites[FieldIdent]]
    val exprWrites = implicitly[JavaMapWrites[SqlExpr]]
    val values = new util.HashMap[String, Any]()
    assigns.foreach { a =>
      val lhs = fieldIdentWrites.writes(a.lhs)
      val rhs = exprWrites.writes(a.rhs)
      values.put(lhs.toString, rhs)
    }
    values
  }

  val exprWrites = SqlExprWrites

  implicit val insRowWrites: JavaMapWrites[InsRow] = writes[InsRow] {
    case com.github.stephentu.scalasqlparser.Set(assigns, _) =>
      assignsToJavaMap(assigns)
    case com.github.stephentu.scalasqlparser.Values(bindings, _) =>
      val values = new util.HashMap[String, Any]()
      bindings.foreach { b =>
        val sym = b.symbol.get
        values.put(sym.column, exprWrites.writes(b.value))
      }
      values
    case com.github.stephentu.scalasqlparser.NamedValues(bindings, _) =>
      val values = new util.HashMap[String, Any]()
      bindings.foreach { b =>
        val sym = b.field.symbol.get
        values.put(sym.column, exprWrites.writes(b.value))
      }
      values
  }

}
