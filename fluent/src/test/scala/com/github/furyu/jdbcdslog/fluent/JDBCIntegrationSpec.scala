package com.github.furyu.jdbcdslog.fluent

import org.specs2.mutable._
import java.sql.{Connection, PreparedStatement, DriverManager}
import org.jdbcdslog.PreparedStatementLoggingHandler

object JDBCIntegrationSpec extends Specification {

  "FluentEventHandler" should {
    "send a valid data to a fluentd" in {

      DriverManager.setLogWriter(new java.io.PrintWriter(System.out))

      ConnectionProvider.provider = Option(
        new ConnectionProvider {
          def withConnection[T](url: String)(block: (Connection) => T): T = {
            block(new com.mysql.jdbc.Driver().connect(url, new java.util.Properties()))
          }
        }
      )

      Class.forName("org.jdbcdslog.DriverLoggingProxy")
      Class.forName("com.mysql.jdbc.Driver")

      val conn = DriverManager.getConnection("jdbc:jdbcdslog:mysql://localhost:3306/test?targetDriver=com.mysql.jdbc.Driver")
      val sql = "update foo set foo = ?, bar = 1"
      val stmt2 = conn.prepareStatement(sql)
      stmt2.setNull(1, java.sql.Types.INTEGER)
      stmt2.executeUpdate()

      success
    }
  }
}
