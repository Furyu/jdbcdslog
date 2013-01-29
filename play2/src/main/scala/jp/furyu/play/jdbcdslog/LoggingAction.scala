package jp.furyu.play.jdbcdslog

import play.api.mvc._
import jp.furyu.jdbcdslog.fluent.{Context, FluentEventHandler}
import java.util.Date
import scala.collection.JavaConverters._
import org.jdbcdslog.plugin.EventHandlerAPI
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.http.{ContentTypes, HeaderNames}

case class ActionProxy[A, B <: Request[_]](
    logger: AccessLogger = LoggingAction.accessLoggerFromConfiguration,
    eventHandler: FluentEventHandler = LoggingAction.eventHandlerFromConfiguration,
    additions: B => Map[String, AnyRef] = { _: B => Map.empty[String, AnyRef] },
    block: B => Result) {
  def apply(request: B): Result = {
    val requestContext = AccessContext(request, additions(request))

    logger.log(requestContext)

    val result = eventHandler.withContext(requestContext) {
      block(request)
    }

    result match {
      case r @ SimpleResult(ResponseHeader(status, headers), body) =>
        val consume = Iteratee.fold[r.BODY_CONTENT, Array[Byte]](Array.empty) { (result, chunk) =>
          result ++ r.writeable.transform(chunk)
        }
        //        val consume = Iteratee.consume[Array[Byte]]()

        val ContentTypeJSON = ContentTypes.JSON
        headers.get(HeaderNames.CONTENT_TYPE) match {
          case Some(ContentTypeJSON) =>
            body(consume).flatMap(_.run).onRedeem { bytes =>
              val bodyAsStr = new String(bytes, "UTF-8")
              val responseContextAdditions = Map(
                "response" -> Map(
                  "status" -> status,
                  "headers" -> headers.map(t => t._1.toLowerCase -> t._2).asJava,
                  "body" -> bodyAsStr
                ).asJava
              )
              val responseContext = AccessContext(
                request = request,
                additions = requestContext.additions ++ responseContextAdditions
              )
              logger.log(responseContext)
            }
          case _ =>
            ;
        }
      //      case r: ChunkedResult =>
      //        ;
      //      case r: AsyncResult =>
      //        ;
      //      case r: PlainResult =>
      //        ;
      //      case r: Result =>
      //        ;
      case _ =>
        ;
    }

    result
  }
}

/**
 * DB操作などのイベントの文脈情報
 * 例えば、XXというテーブルにinsertしたときのアクセス先アクションはどこだったか、というような文脈をログデータに残すために利用されます。
 * 具体的には、リクエストの基本情報(HTTPメソッド、リクエストパス、リクエストパラメータ、タイムスタンプ、リクエストヘッダ)と任意の付加情報(additions)
 * を含みます。
 * @param request このリクエストからHTTPメソッドなどの基本情報を自動的に取得します
 * @param additions 任意の付加情報です。例えば、認証済みアクションからログに出力するためのアクセス元ユーザIDを渡すような場合に利用します
 * @tparam A
 */
case class AccessContext[A<:Request[_]](request: A, additions: Map[String, AnyRef] = Map.empty[String, AnyRef]) extends Context {
  // fluent-java-logger can't serialize Scala's Map nor Seq.
  val queryStringAsJavaMap = request.queryString.map { case (key, value) =>
    (key, value.asJava)
  }.asJava

  def toMap: Map[String, AnyRef] = Map(
    "action" -> (
      Map(
        "method" -> request.method,
        "path" -> request.path,
        "params" -> queryStringAsJavaMap,
        "timestamp" -> new Date().getTime.asInstanceOf[AnyRef]
      ) ++ Map(
        "headers" -> request.headers.toMap.map(t => t._1.toLowerCase -> t._2.headOption.orNull).asJava
      )
    ).asJava
  ) ++ additions

}

/**
 * LoggingAction logs every invocation of action, and every executed SQL statements in it.
 */
class LoggingAction[A](
                        val block: Request[A] => Result,
                        val additions: Request[A] => Map[String, AnyRef] = { _: Request[A] => Map.empty[String, AnyRef] },
                        val parser: BodyParser[A],
                        val logger: AccessLogger,
                        val eventHandler: FluentEventHandler)
  extends Action[A] {

  def apply(request: Request[A]): Result = {
    ActionProxy[A, Request[A]](logger, eventHandler, additions, block)(request)
  }
}

object LoggingAction {
  /**
   * Creates a LoggingAction
   * @param logger Specify your own logger if needed.
   *               The default logger is configured via application.conf. See docs for AccessLogger for more info.
   * @param eventHandler Specify your own event handler if needed.
   *                     The default FluentEventHandler is taken from JDBCDSLogPlugin, and can be configured in jdbcdslog.properties.
   * @param additions Specify our own `additions` function if needed. By default, `additions` function appends each action's:
   *   - Request method (e.g. GET, POST)
   *   - Request path (e.g. /index)
   *   - Request parameters (e.g. {"foo": "bar"} for "?foo=bar")
   *   - Timestamp (The time action is executed in milliseconds from unix epoch)
   * @param block
   * @tparam A
   * @return
   */
  def apply[A](
                parser: BodyParser[A],
                logger: AccessLogger = accessLoggerFromConfiguration,
                eventHandler: FluentEventHandler = eventHandlerFromConfiguration
              )(
                additions: Request[A] => Map[String, AnyRef])(block: Request[A] => Result): LoggingAction[A] =
    new LoggingAction[A](
      parser = parser,
      logger = logger,
      additions = additions,
      eventHandler = eventHandler,
      block = block
    )

  /**
   * Creates a LoggingAction with the default `anyContent` body parser
   * @param logger Specify your own logger if needed.
   *               The default logger is configured via application.conf. See docs for AccessLogger for more info.
   * @param eventHandler Specify your own event handler if needed.
   *                     The default FluentEventHandler is taken from JDBCDSLogPlugin, and can be configured in jdbcdslog.properties.
   * @param additions Specify our own `additions` function if needed. By default, `additions` function appends each action's:
   *   - Request method (e.g. GET, POST)
   *   - Request path (e.g. /index)
   *   - Request parameters (e.g. {"foo": "bar"} for "?foo=bar")
   *   - Timestamp (The time action is executed in milliseconds from unix epoch)
   * @param block
   * @return
   */
  def anyContent(
             logger: AccessLogger = accessLoggerFromConfiguration,
             eventHandler: FluentEventHandler = eventHandlerFromConfiguration
           )(
             additions: Request[AnyContent] => Map[String, AnyRef]
           )(block: Request[AnyContent] => Result): LoggingAction[AnyContent] = {

    new LoggingAction[AnyContent](
      parser = BodyParsers.parse.anyContent,
      logger = logger,
      additions = additions,
      eventHandler = eventHandler,
      block = block
    )
  }

  lazy val accessLoggerFromConfiguration = AccessLogger.default

  lazy val eventHandlerFromConfiguration = EventHandlerAPI.getInstance() match {
    case h: FluentEventHandler =>
      h
  }

}
