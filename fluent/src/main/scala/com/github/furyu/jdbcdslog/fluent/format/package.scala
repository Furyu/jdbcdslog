package com.github.furyu.jdbcdslog.fluent

import com.github.furyu.jdbcdslog.fluent.DefaultWrites.{SeqWrites, SqlExprWrites, JavaMapWrites}
import com.github.stephentu.scalasqlparser.SqlExpr

package object format {

  val seqExprWrites = new SeqWrites[SqlExpr]()

  def consume(data: Any): Any = {
    data match {
      case expr: SqlExpr =>
        SqlExprWrites.writes(expr)
      case seq: Seq[_] =>
        seqExprWrites.writes(seq.asInstanceOf[Seq[SqlExpr]])
      case any: Any =>
        any
    }
  }

  def writes[A](block: A => Any): JavaMapWrites[A] = new JavaMapWrites[A] {
    def writes(a: A) = block(a)
  }

}
