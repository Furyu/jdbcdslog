package jp.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser.{StarProj, ExprProj, SqlProj}
import jp.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}
import collection.generic.CanBuildFrom

object proj {

  implicit val sqlProjWrites: JavaMapWrites[SqlProj] = writes[SqlProj] {
    case ExprProj(expr, alias, _) =>
      val b = Seq.newBuilder[(String, Any)]
      b += "expr" -> SqlExprWrites.writes(expr)
      alias foreach { a =>
        b += "alias" -> a
      }
      obj(b.result: _*)
    case StarProj(_) =>
      obj(
        "expr" -> "*"
      )
  }

}
