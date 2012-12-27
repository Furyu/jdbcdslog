package jp.furyu.jdbcdslog.fluent

import jp.furyu.jdbcdslog.fluent.DefaultWrites.{StmtWrites, SeqWrites, SqlExprWrites, JavaMapWrites}
import com.github.stephentu.scalasqlparser.{Stmt, SqlExpr}

package object format {

  val seqExprWrites = new SeqWrites[SqlExpr]()

  def expression(name: String, any: Any) = {
    val data = new java.util.HashMap[String, Any]()
    data.put(name, consume(any))
    data
  }

  def obj(kvs: (String, Any)*) = {
    val data = new java.util.HashMap[String, Any]()
    kvs.foreach {
      case (k, v) =>
        data.put(k, consume(v))
    }
    data
  }

  def consume(data: Any): Any = {
    data match {
      case expr: SqlExpr =>
        SqlExprWrites.writes(expr)
      case stmt: Stmt =>
        StmtWrites.writes(stmt)
      case seq: Seq[_] =>
        seqExprWrites.writes(seq.asInstanceOf[Seq[SqlExpr]])
      case any: Any =>
        any
      case null =>
          null
    }
  }

  def writes[A](block: A => Any): JavaMapWrites[A] = new JavaMapWrites[A] {
    def writes(a: A) = block(a)
  }

}
