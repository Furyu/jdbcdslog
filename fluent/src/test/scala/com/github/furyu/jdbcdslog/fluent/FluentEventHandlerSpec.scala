package com.github.furyu.jdbcdslog.fluent

import org.specs2.mutable._

object FluentEventHandlerSpec extends Specification {

  "FluentEventHandler" should {
    "send a valid data to a fluentd" in {

      val handler = new FluentEventHandler

//      handler.preparedStatement(
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
