package jp.furyu.play.jdbcdslog

import play.api.mvc._
import jp.furyu.jdbcdslog.fluent.{Context, FluentEventHandler}
import java.util.Date
import scala.collection.JavaConverters._
import org.jdbcdslog.plugin.EventHandlerAPI

case class AccessContext[A](request: Request[A], additions: Map[String, AnyRef]) extends Context {
  // fluent-java-logger can't serialize Scala's Map nor Seq.
  val queryStringAsJavaMap = request.queryString.map { case (key, value) =>
    (key, value.asJava)
  }.asJava

  def toMap: Map[String, AnyRef] = Map(
    "api" -> (
      Map(
        "method" -> request.method,
        "path" -> request.path,
        "params" -> queryStringAsJavaMap,
        "timestamp" -> new Date().getTime.asInstanceOf[AnyRef]
      ) ++ Map(
        "headers" -> request.headers.toMap.map(t => t._1 -> t._2.asJava).asJava,
        "languages" -> request.acceptLanguages.map(_.code).asJava
      )
    ).asJava
  ) ++ additions

}

/**
 * LoggingAction logs every invocation of action, and every executed SQL statements in it.
 */
class LoggingAction[A](
                        val block: Request[A] => Result,
                        val additions: Request[A] => Map[String, AnyRef],
                        val parser: BodyParser[A],
                        val logger: AccessLogger,
                        val eventHandler: FluentEventHandler)
  extends Action[A] {

  def apply(request: Request[A]): Result = {
    val context = AccessContext(request, additions(request))

    logger.log(context)

    eventHandler.withContext(context) {
      block(request)
    }
  }
}

object LoggingAction {

  def apply[A](
                parser: BodyParser[A],
                logger: AccessLogger,
                eventHandler: FluentEventHandler,
                additions: Request[A] => Map[String, AnyRef])(block: Request[A] => Result): LoggingAction[A] =
    new LoggingAction[A](
      block = block,
      additions = additions,
      logger = logger,
      parser = parser,
      eventHandler = eventHandler
    )

  /**
   * Creates a LoggingAction with the default `anyContent` body parser
   */
  def apply(
             logger: AccessLogger,
             eventHandler: FluentEventHandler,
             additions: Request[AnyContent] => Map[String, AnyRef])
           (block: Request[AnyContent] => Result): LoggingAction[AnyContent] = {

    new LoggingAction[AnyContent](
      parser = BodyParsers.parse.anyContent,
      block = block,
      logger = logger,
      eventHandler = eventHandler,
      additions = additions
    )
  }

  lazy val accessLoggerFromConfiguration = AccessLogger.default

  lazy val eventHandlerFromConfiguration = EventHandlerAPI.getInstance() match {
    case h: FluentEventHandler =>
      h
  }

  /**
   * Generates a commonly-used LoggingAction with:
   * - `additions` function appends each action's:
   *   - Request method (e.g. GET, POST)
   *   - Request path (e.g. /index)
   *   - Request parameters (e.g. {"foo": "bar"} for "?foo=bar")
   *   - Timestamp (The time action is executed in milliseconds from unix epoch)
   * - The default AccessLogger (which is configured via application.conf. See docs for AccessLogger for more info.)
   * - The default FluentEventHandler (taken from JDBCDSLogPlugin, which can be configured in jdbcdslog.properties)
   * @param bodyParser
   * @param block
   * @tparam A
   * @return
   */
  def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result): LoggingAction[A] = {
    new LoggingAction[A](
      block = block, additions = { request =>
        val queryStringAsJavaMap = request.queryString.map {
          case (key, value) =>
            key -> value.asJava
        }.asJava
        Map(
          "action" -> Map(
            "method" -> request.method,
            "path" -> request.path,
            "params" -> queryStringAsJavaMap,
            "timestamp" -> new Date().getTime.asInstanceOf[AnyRef]
          ).asJava
        )
      }, parser = bodyParser, logger = accessLoggerFromConfiguration, eventHandler = eventHandlerFromConfiguration
    )
  }

  /**
   * Generates a commonly-used LoggingAction with:
   * - `additions` function appends each action's:
   *   - Request method (e.g. GET, POST)
   *   - Request path (e.g. /index)
   *   - Request parameters (e.g. {"foo": "bar"} for "?foo=bar")
   *   - Timestamp (The time action is executed in milliseconds from unix epoch)
   * - The default AccessLogger (which is configured via application.conf. See docs for AccessLogger for more info.)
   * - The default FluentEventHandler (taken from JDBCDSLogPlugin, which can be configured in jdbcdslog.properties)
   * - `AnyContent` body parser
   * @param block
   * @return
   */
  def apply(block: Request[AnyContent] => Result): LoggingAction[AnyContent] = {
    LoggingAction(BodyParsers.parse.anyContent)(block)
  }
}
