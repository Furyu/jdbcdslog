package jp.furyu.play.jdbcdslog

import org.specs2.mutable._
import play.api.mvc._
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._
import org.specs2.mock.Mockito
import jp.furyu.jdbcdslog.fluent.{FluentEventHandler, Context}
import play.api.libs.json.{JsValue, Json}
import org.specs2.specification.Scope
import play.api.libs.iteratee.Enumerator

object LoggingActionSpec extends Specification with Mockito {

  sequential

  abstract class Secured[A](block: Request[A] => Result) extends Action[A] {
    def apply(request: Request[A]): Result = {
      block(request)
    }
  }

  object Secured {
    def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result) =
      new Secured[A](block) {
        def parser: BodyParser[A] = bodyParser
      }
    def apply(block: Request[AnyContent] => Result): Secured[AnyContent] =
      Secured(BodyParsers.parse.anyContent)(block)
  }

  trait runningFakeApp extends Around {
    implicit val fakeApplication = new FakeApplication()

    def around[T](t: => T)(implicit evidence$1: (T) => org.specs2.execute.Result): org.specs2.execute.Result = {
      running(fakeApplication) {
        t
      }
    }
  }

  "LoggingAction" should {

    "send an access log for each request" in new runningFakeApp {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def foo = LoggingAction.anyContent(logger, eventHandler)(req => additions) { request =>
          Ok("executed")
        }
      }

      // each matcher must be a function
      def beOk: SimpleResult[String] = beLike[SimpleResult[String]] {
        case result => status(result) must be equalTo (200)
      }

      controller.eventHandler.withContext(context)(beOk) returns controller.Ok("eventHandler")

      val result = controller.foo()(request)

      status(result) must be equalTo (200)

      there was one(logger).log(context) then
        one(controller.eventHandler).withContext(context)(beOk)
    }

    "be nested inside other action" in new runningFakeApp {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def foo = Secured { request =>
          LoggingAction.anyContent(logger, eventHandler)(req => additions) { request =>
            Ok("foo")
          } apply (request)
        }
      }

      // each matcher must be a function
      def beOk: SimpleResult[String] = beLike[SimpleResult[String]] {
        case result => {
          status(result) must be equalTo (200)
          contentAsString(result) must be equalTo ("foo")
        }
      }

      controller.eventHandler.withContext(context)(beOk) returns controller.Ok("foo1")

      val resultFoo = controller.foo()(request)

      status(resultFoo) must be equalTo (200)
      contentAsString(resultFoo) must be equalTo ("foo1")

      there was one(logger).log(context) then
        one(controller.eventHandler).withContext(context)(beOk)
    }

    "nest other action" in new runningFakeApp {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def bar = LoggingAction[AnyContent](BodyParsers.parse.anyContent, logger, eventHandler)(req => additions) { request =>
          Secured { request =>
            Ok("bar")
          } apply (request)
        }
      }

      // each matcher must be a function
      def beOk: SimpleResult[String] = beLike[SimpleResult[String]] {
        case result => {
          status(result) must be equalTo (200)
          contentAsString(result) must be equalTo ("bar")
        }
      }

      controller.eventHandler.withContext(context)(beOk) returns controller.Ok("bar1")

      val resultBar = controller.bar()(request)

      status(resultBar) must be equalTo (200)
      contentAsString(resultBar) must be equalTo ("bar1")

      there was one(logger).log(context) then
        one(controller.eventHandler).withContext(context)(beOk)
    }

    "be disabled" in {
      implicit val app = FakeApplication(additionalConfiguration = Map("jdbcdslogplugin.disabled" -> "true"))
      running(app) {
        def ok(body: String): SimpleResult[String] = {
          SimpleResult(ResponseHeader(OK), Enumerator(body))
        }

        val ok1 = ok("1")
        val ok2 = ok("2")

        val logger = mock[AccessLogger]
        val additions = { req: FakeRequest[AnyContent] => Map("foo" -> "bar") }
        val eventHandler = mock[FluentEventHandler].defaultReturn(ok2)
        val block = { req: FakeRequest[AnyContent] => ok1 }
        val proxy = ActionProxy(logger, eventHandler, additions = additions, block)
        val request = FakeRequest("GET", "/json?a=b")
        val context = AccessContext(request, additions(request))
        val actual = proxy(request)

        status(actual) must be equalTo (OK)
        contentAsString(actual) must be equalTo ("1")

        def beOk: SimpleResult[String] = beLike[SimpleResult[String]] {
          case result => {
            status(result) must be equalTo (200)
          }
        }

        there was no(logger).log(any[Context])
        there was no(eventHandler).withContext(context)(any[SimpleResult[String]])
      }
    }

    "log JSON responses" in new runningFakeApp {
      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/json?a=b")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val requestContext = AccessContext(
        request = request,
        additions = additions
      )

      import collection.JavaConverters._

      val responseContext = AccessContext(
        request = request,
        additions = Map(
          "foo" -> "bar",
          "response" -> Map(
            "status" -> 200,
            "headers" -> Map("content-type" -> "application/json; charset=utf-8").asJava,
            "body" -> """{"ok":1}"""
          ).asJava)
      )

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        val response = Ok(Json.toJson(Map("ok" -> 1)))
        def foo = LoggingAction.anyContent(logger, eventHandler)(req => additions) { request =>
          response
        }
      }

      // each matcher must be a function
      def beOk: SimpleResult[JsValue] = beLike[SimpleResult[JsValue]] {
        case result => status(result) must be equalTo (200)
      }

      def eq[A](expectedValue: A): A = beLike[A] {
        case result => result must be equalTo (expectedValue)
      }

      def eqs(values: List[AccessContext[FakeRequest[AnyContent]]]): AccessContext[FakeRequest[AnyContent]] = beLike[AccessContext[FakeRequest[AnyContent]]] {
        case result => values must haveOneElementLike { case elem => result must beEqualTo(elem) }
      }

      def beReqOrResContext: AccessContext[FakeRequest[AnyContent]] = beLike[AccessContext[FakeRequest[AnyContent]]] {
        //
        case result => result must beEqualTo (responseContext) or beEqualTo (requestContext)
      }

      controller.eventHandler.withContext(context)(beOk) returns controller.response

      val result = controller.foo()(request)

      status(result) must be equalTo (200)
      // Here, we await for the response body is consumed, by `contentAsString'.
      // A few moments later, the response body is sent to mocked logger.
      contentAsString(result) must be equalTo ("""{"ok":1}""")

      there was two(logger).log(beReqOrResContext) then
        one(controller.eventHandler).withContext(context)(beOk)
    }

    "log JSON response without an action" in {
      val userId = 100

      case class AuthorizedRequest[A](underlying: Request[A], userId: Int) extends WrappedRequest[A](underlying)

      case class Authorized[A](parser: BodyParser[A])(block: AuthorizedRequest[A] => Result) extends Action[A] {
        override def apply(request: Request[A]): Result = {
          block(AuthorizedRequest(request, userId))
        }
      }

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/json?a=b")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(AuthorizedRequest(request, userId), additions)

      val requestContext = AccessContext(
        request = request,
        additions = additions
      )

      import collection.JavaConverters._

      val responseContext = AccessContext(
        request = request,
        additions = Map(
          "foo" -> "bar",
          "response" -> Map(
            "status" -> 200,
            "headers" -> Map("content-type" -> "application/json; charset=utf-8").asJava,
            "body" -> """{"ok":1}"""
          ).asJava)
      )

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        val response = Ok(Json.toJson(Map("ok" -> 1)))
        val proxy = LoggingActionProxy[AnyContent, AuthorizedRequest[AnyContent]](logger, eventHandler, (req: AuthorizedRequest[AnyContent]) => additions, { request: AuthorizedRequest[AnyContent] => response })
        def foo = Authorized(BodyParsers.parse.anyContent) { request =>
          proxy(request)
        }
      }

      // each matcher must be a function
      def beOk: SimpleResult[JsValue] = beLike[SimpleResult[JsValue]] {
        case result => status(result) must be equalTo (200)
      }

      def beReqOrResContext: AccessContext[FakeRequest[AnyContent]] = beLike[AccessContext[FakeRequest[AnyContent]]] {
        //
        case result => result.additions must beEqualTo (responseContext.additions) or beEqualTo (requestContext.additions)
      }

      controller.eventHandler.withContext(context)(beOk) returns controller.response

      val result = controller.foo()(request)

      status(result) must be equalTo (200)
      // Here, we await for the response body is consumed, by `contentAsString'.
      // A few moments later, the response body is sent to mocked logger.
      contentAsString(result) must be equalTo ("""{"ok":1}""")

      there were two(logger).log(beReqOrResContext) then
        one(controller.eventHandler).withContext(context)(beOk)
    }
  }

}
