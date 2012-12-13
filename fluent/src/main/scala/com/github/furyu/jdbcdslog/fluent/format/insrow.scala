package com.github.furyu.jdbcdslog.fluent.format

import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SqlExprWrites, JavaMapWrites}
import com.github.stephentu.scalasqlparser.InsRow
import java.util

object insrow {

  implicit val insRowWrites: JavaMapWrites[InsRow] = writes[InsRow] {
    case com.github.stephentu.scalasqlparser.Set(assigns, _) =>
      val set = new util.ArrayList[Any]()
      assigns.foreach { assign =>
        set.add(com.github.furyu.jdbcdslog.fluent.DefaultWrites.assignWrites.writes(assign))
      }
      set
    case com.github.stephentu.scalasqlparser.Values(values, _) =>
      val vs = new util.ArrayList[Any]()
      values.foreach { value =>
        vs.add(SqlExprWrites.writes(value))
      }
    vs
  }

}
