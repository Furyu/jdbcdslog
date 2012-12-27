package jp.furyu.jdbcdslog.fluent

trait Context {
  def toMap: Map[String, AnyRef]
}
