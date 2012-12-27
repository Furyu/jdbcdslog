package jp.furyu.jdbcdslog.fluent

import org.specs2.mutable._

object FluentEventHandlerSpec extends Specification {

  "FluentEventHandler" should {
    "send a valid data to a fluentd" in {

      val props = new java.util.Properties()
      props.put("jdbcdslog.fluent.host", "localhost")
      props.put("jdbcdslog.fluent.port", "24224")
      props.put("jdbcdslog.fluent.tag", "tag1")
      props.put("jdbcdslog.fluent.label", "label1")

      val handler = new FluentEventHandler(props)

//      handler.statement(
//        """
//          |update posts p
//          |set p.value = (1 + 2) * 3 / 4
//          |where exists (
//          |select * from comments c
//          |where p.id = c.post_id
//          |)
//        """.stripMargin, null, 123456789L)

      success
    }
  }

}
