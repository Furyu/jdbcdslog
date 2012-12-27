package jp.furyu.play.jdbcdslog

import jp.furyu.play.jdbcdslog.{AccessLogger, AccessContext, LoggingAction}
import org.specs2.mutable._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.specs2.mock.Mockito
import com.github.furyu.jdbcdslog.fluent.{FluentEventHandler, Context}

object LoggingActionSpec extends Specification with Mockito {

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

  "LoggingAction" should {

    "send an access log for each request" in {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def foo = LoggingAction(logger, eventHandler, additions) { request =>
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

    "be nested inside other action" in {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def foo = Secured { request =>
          LoggingAction(logger, eventHandler, additions) { request =>
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

    "nest other action" in {

      val logger = mock[AccessLogger]

      val request = FakeRequest("GET", "/foo?bar=baz")
      val additions = Map("foo" -> "bar")
      val context = AccessContext(request, additions)

      val controller = new Controller {
        val eventHandler = mock[FluentEventHandler].defaultReturn(Ok("stubbed"))
        def bar = LoggingAction(logger, eventHandler, additions) { request =>
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
  }

}
