package com.github.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import com.github.furyu.jdbcdslog.fluent.DefaultWrites.JavaMapWrites

object literal {

  def writes[A](block: A => Any): JavaMapWrites[A] = new JavaMapWrites[A] {
    def writes(a: A) = block(a)
  }

  implicit val dateLiteralWrites = writes[DateLiteral] { case DateLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("date", d)
    data
  }

  implicit val floatLiteralWrites = writes[FloatLiteral] { case FloatLiteral(v, _) =>
    v
  }

  implicit val intLiteralWrites = writes[IntLiteral] { case IntLiteral(v, _) =>
    v
  }

  implicit val intervalWrites = writes[IntervalLiteral] { case IntervalLiteral(date, unit, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("date", date)
    data.put("unit", unit)
    data
  }

  implicit val nullLiteralWrites = writes[NullLiteral] { case NullLiteral(_) =>
    null
  }

  implicit val stringLiteralWrites = writes[StringLiteral] { case StringLiteral(v, _) =>
    v
  }

}
