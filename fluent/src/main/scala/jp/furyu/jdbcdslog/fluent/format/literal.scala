package jp.furyu.jdbcdslog.fluent.format

import com.github.stephentu.scalasqlparser._
import jp.furyu.jdbcdslog.fluent.DefaultWrites.JavaMapWrites

object literal {

  def writes[A](block: A => Any): JavaMapWrites[A] = new JavaMapWrites[A] {
    def writes(a: A) = block(a)
  }

  implicit val dateLiteralWrites = writes[DateLiteral] { case DateLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("date", d)
    data
  }

  implicit val timeLiteralWrites = writes[TimeLiteral] { case TimeLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("time", d)
    data
  }

  implicit val timestampLiteralWrites = writes[TimestampLiteral] { case TimestampLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("timestamp", d)
    data
  }

  implicit val odbcDateLiteralWrites = writes[ODBCDateLiteral] { case ODBCDateLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("date", d)
    data
  }

  implicit val odbcTimeLiteralWrites = writes[ODBCTimeLiteral] { case ODBCTimeLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("time", d)
    data
  }

  implicit val odbcTimestampLiteralWrites = writes[ODBCTimestampLiteral] { case ODBCTimestampLiteral(d, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("timestamp", d)
    data
  }

  implicit val floatLiteralWrites = writes[FloatLiteral] { case FloatLiteral(v, _) =>
    v
  }

  implicit val intLiteralWrites = writes[IntLiteral] { case IntLiteral(v, _) =>
    v.toString
  }

  implicit val intervalWrites = writes[IntervalLiteral] { case IntervalLiteral(date, unit, _) =>
    val data = new java.util.HashMap[String, AnyRef]()
    data.put("date", date)
    data.put("unit", unit.toString)
    data
  }

  implicit val nullLiteralWrites = writes[NullLiteral] { case NullLiteral(_) =>
    null
  }

  implicit val stringLiteralWrites = writes[StringLiteral] { case StringLiteral(v, _) =>
    v
  }

}
