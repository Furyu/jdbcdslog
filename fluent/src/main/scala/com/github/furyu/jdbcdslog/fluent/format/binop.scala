package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}

object binop {

  implicit object EqWrites extends JavaMapWrites[Eq] {
    def writes(a: Eq) = {
      val Eq(lhs, rhs, _) = a
      val data = new java.util.HashMap[String, Any]()
      data.put("lhs", SqlExprWrites.writes(lhs))
      data.put("rhs", SqlExprWrites.writes(rhs))
      data
    }
  }

}
