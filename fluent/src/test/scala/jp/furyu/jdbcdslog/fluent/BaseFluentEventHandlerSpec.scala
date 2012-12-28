package jp.furyu.jdbcdslog.fluent

import org.specs2.mutable._
import org.fluentd.logger.FluentLogger
import org.specs2.mock.Mockito
import com.github.stephentu.scalasqlparser.Definitions

object BaseFluentEventHandlerSpec extends Specification with Mockito {

  "BaseFluentEventHandler" should {
    "log SQL statements" in {
      val c = new Context {
        def toMap: Map[String, AnyRef] = Map(
          "a" -> "b"
        )
      }
      val logger = mock[FluentLogger]
      val factory = mock[SchemaFactory]
      val marshaller = mock[FluentMarshaller]
      val handler = new BaseFluentEventHandler {
        def context: Option[Context] = Some(c)

        val fluentLogger: FluentLogger = logger
        val fluentMarshaller: FluentMarshaller = marshaller
        val label: String = "label1"
        val schemaFactory: SchemaFactory = factory
      }
      val sql = "select * from users"
      val stmt = mock[java.sql.PreparedStatement]
      val schema = mock[Definitions]
      val data = new java.util.HashMap[String, AnyRef]
      factory.schemaFor(stmt) returns schema
      handler.fluentMarshaller.marshal(sql, schema, Some(c), 123L) returns data

      handler.statement(stmt, sql, new java.util.HashMap[Any, Any](), 123L)

      there was one(factory).schemaFor(stmt) then
        one(marshaller).marshal(sql, schema, Some(c), 123L) then
        one(logger).log(handler.label, data)
    }
  }

}
