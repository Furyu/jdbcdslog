Fluentd Plugin for jdbcdslog
----------------------------

An jdbcdslog plugin to send every sql statements executed through JDBC to Fluentd.

## Prerequisites

This plugin is for the forked version(https://github.com/furyu/jdbcdslog) of jdbcdslog,
not for the original jdbcdslog(http://code.google.com/p/jdbcdslog/) nor
jdbcdslog-exp(https://github.com/jdbcdslog/jdbcdslog).

## Configuration

You must add two dependencies to your build settings at first:

  libraryDependencies ++= Seq(
    "jp.furyu.jdbcdslog" %% "jdbcdslog-fluent" % "0.1-SNAPSHOT",
    "jp.furyu.jdbcdslog" %% "jdbcdslog-play2" % "0.1-SNAPSHOT"
  )

In conf/jdbcdslog.properties:

  jdbcdslog.pluginClassName=jp.furyu.jdbcdslog.fluent.FluentEventHandler
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

### Logging

jdbcdslog depends on slf4j which requires logging implementation e.g. logback, log4j, etc at run time.
But you don't need to add logback or log4j explicitly your dependencies because
logback is already included in Play2's dependencies.
To enable logback logging for jdbcdslog-play2, refer to the documentation of Play2 and logback and provided
appropriate configurations.
