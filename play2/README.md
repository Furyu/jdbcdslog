= Configuration

You must add two dependencies to your build settings at first:

  libraryDependencies ++= Seq(
    "jp.furyu.jdbcdslog" %% "jdbcdslog-fluent" % "0.1-SNAPSHOT",
    "jp.furyu.jdbcdslog" %% "jdbcdslog-play2" % "0.1-SNAPSHOT"
  )

In conf/jdbcdslog.properties:

  jdbcdslog.pluginClassName=com.github.furyu.jdbcdslog.fluent.FluentEventHandler
  jdbcdslog.driverName=mysql
  // or else the database you use
  // jdbcdslog.driverName=oracle

In conf/play.plugins:

  201:jp.furyu.play.jdbcdslog.JDBCDSLogPlugin
