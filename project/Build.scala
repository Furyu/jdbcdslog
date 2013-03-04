import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "jdbcdslog"
  val appVersion = "0.2.4"
  val baseName = "jdbcdslog"
  val jdbcdslogOrg = "jp.furyu.jdbcdslog"
  val play2JdbcdslogOrg = "jp.furyu.play2"

  val defaultSettings = Seq(
    scalacOptions ++= Seq("-encoding", "UTF-8", "-Xlint","-deprecation", "-unchecked"),
    // Ensure that you run sbt with JDK6
    javacOptions ++= Seq("-source","1.6","-target","1.6", "-encoding", "UTF-8", "-Xlint")
  )

  val logbackDependenciesInTest = Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.9" % "test",
    "ch.qos.logback" % "logback-core" % "1.0.9" % "test"
  )

  def scalaBaseSettings(componentName: String) = Defaults.defaultSettings ++ defaultSettings /*++ ScctPlugin.instrumentSettings*/ ++ Seq(
    crossScalaVersions := Seq("2.9.1", "2.9.2")
//    crossVersion := CrossVersion.full,
//    parallelExecution in Test := false,
//    parallelExecution in ScctPlugin.ScctTest := false
//    ScctPlugin.scctReportDir := file("/var/www/html/" + appName + "-" + componentName)
  )

  lazy val root = Project("root", base = file("."))
    .settings(
    crossScalaVersions := Seq("2.9.1", "2.9.2"),
    publish := {},
    publishLocal := {}
    ).aggregate(core, slf4j, fluent, play2)

  lazy val core = Project(baseName + "-core", base = file("core")).settings(defaultSettings:_*).settings(
    organization := jdbcdslogOrg,
    version := appVersion,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.2",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "junit" % "junit" % "4.9" % "test",
      "hsqldb" % "hsqldb" % "1.8.0.10" % "test",
      "com.novocode" % "junit-interface" % "0.10-M2" % "test",
      "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5" % "test",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "mysql" % "mysql-connector-java" % "5.1.18" % "test"
    ) ++ logbackDependenciesInTest
  )

  lazy val slf4j = Project(baseName + "-slf4j", base = file("slf4j")).settings(
    organization := jdbcdslogOrg,
    version := appVersion,
    publish := {},
    publishLocal := {},
    resolvers ++= Seq(
    ),
    libraryDependencies ++= Seq(
    )
  )

  lazy val fluent = Project(baseName + "-fluent", base = file("fluent")).settings(scalaBaseSettings("fluent"):_*).settings(
    organization := jdbcdslogOrg,
    version := appVersion,
    resolvers ++= Seq( 
      "Fluent Maven2 Repository" at "http://fluentd.org/maven2"
    ),
    libraryDependencies ++= Seq(
      "org.fluentd" % "fluent-logger" % "0.2.4",
      "jp.furyu.scalasqlparser" % "scala-sql-parser_2.9.2" % "0.1.4",
      "mysql" % "mysql-connector-java" % "5.1.18" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5" % "test",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.11" % "test",
      "org.codehaus.jackson" % "jackson-core-asl" % "1.9.11" % "test"
    ) ++ logbackDependenciesInTest
  ) dependsOn (core)

  // The project to test jdbcdslog-fluent in Scala console.
  // You need compile dependency to slf4j-log4j and log4j to test it in Scala console.
  lazy val fluentConsole = {
    Project("fluent-console", base = file("fluent-console")).settings(
      organization := jdbcdslogOrg,
      version := appVersion,
      publish := {},
      publishLocal := {},
      libraryDependencies ++= Seq(
        "mysql" % "mysql-connector-java" % "5.1.18",
        "ch.qos.logback" % "logback-classic" % "1.0.9",
        "ch.qos.logback" % "logback-core" % "1.0.9"
      )
    ) dependsOn (fluent)
  }

  lazy val play2 = Project("play2-" + baseName, base = file("play2")).settings(scalaBaseSettings("play2"):_*).settings(
    organization := play2JdbcdslogOrg,
    version := appVersion,
    libraryDependencies ++= Seq(
      "play" % "play_2.9.1" % "2.0.4",
      "play" % "play-test_2.9.1" % "2.0.4" % "test",
      "org.specs2" %% "specs2" % "1.12.3" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.hamcrest" % "hamcrest-all" % "1.1" % "test"
    )
  ) dependsOn (fluent)

}
