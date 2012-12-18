= Configuration

You must add two dependencies to your build settings at first:

  libraryDependencies ++= Seq(
    "jp.furyu.jdbcdslog" %% "jdbcdslog-fluent" % "0.1-SNAPSHOT",
    "jp.furyu.jdbcdslog" %% "jdbcdslog-play2" % "0.1-SNAPSHOT"
  )

In conf/jdbcdslog.properties:

  jdbcdslog.pluginClassName=com.github.furyu.jdbcdslog.fluent.FluentEventHandler
  // The jdbc driver name to switch between DB specifics (See MySqlRdbmsSpecifics, OracleRdbmsSpecifics for details)
  jdbcdslog.driverName=mysql
  // or else the database you use
  // jdbcdslog.driverName=oracle
  jdbcdslog.fluent.tag=debug
  jdbcdslog.fluent.label=test
  jdbcdslog.fluent.host=localhost
  jdbcdslog.fluent.port=24224

In conf/play.plugins:

  201:jp.furyu.play.jdbcdslog.JDBCDSLogPlugin

jdbcdslog depends on slf4j which requires logging implementation e.g. logback, log4j, etc at run time.
But you don't need to add logback or log4j explicitly your dependencies because
logback is already included in Play2's dependencies.
