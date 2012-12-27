package jp.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser.{Substring, FunctionCall, Extract}

object function {

  def fun(name: String, options: (String, Any)*) = {
    val fun = new java.util.HashMap[String, Any]()
    options.foreach {
      case (key, any) =>
        fun.put(key, consume(any))
    }

    fun.put("name", name)

    val data = new java.util.HashMap[String, Any]()
    data.put("function", fun)
    data
  }

  implicit val extractWrites = writes[Extract] {
    case Extract(expr, tpe, _) =>
      fun("extract", "expr" -> expr, "type" -> tpe.toString.toLowerCase)
  }

  implicit val substringWrites = writes[Substring] {
    case Substring(expr, from, length, _) =>
      fun("substring", "expr" -> expr, "length" -> length)
  }

  implicit val functionCallWrites = writes[FunctionCall] {
    case FunctionCall(name, args, _) =>
      fun(name, "args" -> args)
  }

}
