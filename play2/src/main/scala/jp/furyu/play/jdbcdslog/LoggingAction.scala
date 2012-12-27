package jp.furyu.play.jdbcdslog

import play.api.mvc._
import jp.furyu.jdbcdslog.fluent.{Context, FluentEventHandler}
import java.util.Date
import scala.collection.JavaConverters._
import jp.furyu.play.jdbcdslog.AccessLogger

case class AccessContext[A](request: Request[A], additions: Map[String, AnyRef]) extends Context {
  // fluent-java-logger can't serialize Scala's Map nor Seq.
  val queryStringAsJavaMap = request.queryString.map { case (key, value) =>
    (key, value.asJava)
  }.asJava

  def toMap: Map[String, AnyRef] = Map(
    "api" -> Map(
      "method" -> request.method,
      "path" -> request.path,
      "params" -> queryStringAsJavaMap,
      "timestamp" -> new Date().getTime.asInstanceOf[AnyRef]
    ).asJava
  ) ++ additions

}

/**
 * 実行されたSQL文のログをとるためのアクション
 */
abstract class LoggingAction[A](block: Request[A] => Result, additions: => Map[String, AnyRef]) extends Action[A] {

  def logger: AccessLogger

  def eventHandler: FluentEventHandler

  def apply(request: Request[A]): Result = {
    val context = AccessContext(request, additions)

    logger.log(context)

    eventHandler.withContext(context) {
      block(request)
    }
  }
}

object LoggingAction {

  def apply[A](bodyParser: BodyParser[A])(accessLogger: AccessLogger, fluentEventHandler: FluentEventHandler, additions: => Map[String, AnyRef])(block: Request[A] => Result): LoggingAction[A] =
    new LoggingAction[A](block, additions) {
      def logger = accessLogger
      def parser = bodyParser
      val eventHandler = fluentEventHandler
    }

  /**
   * Creates a LoggingAction with the default `anyContent` body parser
   */
  def apply(accessLogger: AccessLogger, fluentEventHandler: FluentEventHandler, additions: => Map[String, AnyRef])(block: Request[AnyContent] => Result): LoggingAction[AnyContent] =
    LoggingAction(BodyParsers.parse.anyContent)(
      accessLogger = accessLogger,
      fluentEventHandler = fluentEventHandler,
      additions = additions
    )(block)
}
