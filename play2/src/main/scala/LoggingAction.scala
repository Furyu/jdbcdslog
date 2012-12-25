package jp.furyu.play.jdbcdslog

import play.api.mvc._
import com.github.furyu.jdbcdslog.fluent.{Context, FluentEventHandler}
import java.util.Date
import scala.collection.JavaConverters._

/**
 * 実行されたSQL文のログをとるためのアクション
 */
abstract class LoggingAction[A](block: Request[A] => Result, userId: => Long, segmentId: => Int) extends Action[A] {

  def logger: AccessLogger

  val eventHandler = org.jdbcdslog.plugin.EventHandlerAPI.getInstance().asInstanceOf[FluentEventHandler]

  def apply(request: Request[A]): Result = {
    // fluent-java-logger can't serialize Scala's Map nor Seq.
    val queryStringAsJavaMap = request.queryString.map { case (key, value) =>
      (key, value.asJava)
    }.asJava

    val context = new Context {

      def toMap: Map[String, AnyRef] = Map(
        "api" -> Map(
          "method" -> request.method,
          "path" -> request.path,
          "params" -> queryStringAsJavaMap,
          "timestamp" -> new Date().getTime.asInstanceOf[AnyRef]
        ).asJava,
        "service_id" -> "cardgame",
        "user_id" -> userId.asInstanceOf[AnyRef],
        "segment_id" -> segmentId.asInstanceOf[AnyRef]
      )
    }

    logger.log(context)

    eventHandler.withContext(context) {
      block(request)
    }
  }
}

object LoggingAction {

  def apply[A](bodyParser: BodyParser[A])(userId: => Long, segmentId: => Int)(block: Request[A] => Result): LoggingAction[A] =
    new LoggingAction[A](block, userId, segmentId) {
      def logger = AccessLogger.default
      def parser = bodyParser
    }

  def apply(userId: => Long, segmentId: => Int)(block: Request[AnyContent] => Result): LoggingAction[AnyContent] =
    apply[AnyContent](BodyParsers.parse.anyContent)(userId, segmentId)(block)
}
