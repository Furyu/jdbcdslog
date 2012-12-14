import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "jdbcdslog"
  val appVersion = "0.1-SNAPSHOT"
  val baseName = "jdbcdslog"
  val appOrganization = "jp.furyu.jdbcdslog"

  val defaultSettings = Seq(
    crossScalaVersions := Seq("2.9.1", "2.9.2")
  )

  val log4jDependenciesInTest = Seq(
    "org.slf4j" % "slf4j-log4j12" % "1.5.10" % "test" exclude(
      "javax.jms", "jms"
      ) exclude(
      "javax.mail" , "mail"
      ) exclude(
      "com.sun.jdmk", "jmxtools"
      ) exclude(
      "com.sun.jmx", "jmxri"
      ),
    "log4j" % "log4j" % "1.2.14" % "test" exclude(
      "javax.jms", "jms"
      ) exclude(
      "javax.mail" , "mail"
      ) exclude(
      "com.sun.jdmk", "jmxtools"
      ) exclude(
      "com.sun.jmx", "jmxri"
      )
  )

  lazy val root = Project("root", base = file("."))
    .settings(defaultSettings:_*)
    .settings(publish := {}, publishLocal := {})
    .aggregate(core, slf4j, fluent)

  lazy val core = Project(baseName + "-core", base = file("core")).settings(
    organization := appOrganization,
    version := appVersion,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.5.10",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "junit" % "junit" % "4.9" % "test",
      "hsqldb" % "hsqldb" % "1.8.0.10" % "test",
      "com.novocode" % "junit-interface" % "0.10-M2" % "test",
      "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5" % "test",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "mysql" % "mysql-connector-java" % "5.1.18" % "test"
    ) ++ log4jDependenciesInTest
  )

  lazy val slf4j = Project(baseName + "-slf4j", base = file("slf4j")).settings(
    organization := appOrganization,
    version := appVersion,
    publish := {},
    publishLocal := {},
    resolvers ++= Seq(
    ),
    libraryDependencies ++= Seq(
    )
  )

  lazy val fluent = Project(baseName + "-fluent", base = file("fluent")).settings(
    organization := appOrganization,
    version := appVersion,
    resolvers ++= Seq( 
      "Fluent Maven2 Repository" at "http://fluentd.org/maven2"
    ),
    libraryDependencies ++= Seq(
      "org.fluentd" % "fluent-logger" % "0.2.4",
      "com.github.stephentu.scalasqlparser" % "scala-sql-parser_2.9.2" % "0.1-SNAPSHOT",
      "mysql" % "mysql-connector-java" % "5.1.18" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5" % "test",
      "org.specs2" %% "specs2" % "1.9" % "test"
    ) ++ log4jDependenciesInTest
  ) dependsOn (core)

  // The project to test jdbcdslog-fluent in Scala console.
  // You need compile dependency to slf4j-log4j and log4j to test it in Scala console.
  lazy val fluentConsole = Project("fluent-console", base = file("fluent-console")).settings(
    organization := appOrganization,
    version := appVersion,
    publish := {},
    publishLocal := {},
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-log4j12" % "1.5.10" exclude(
        "javax.jms", "jms"
        ) exclude(
        "javax.mail" , "mail"
        ) exclude(
        "com.sun.jdmk", "jmxtools"
        ) exclude(
        "com.sun.jmx", "jmxri"
        ),
      "log4j" % "log4j" % "1.2.14" exclude(
        "javax.jms", "jms"
        ) exclude(
        "javax.mail" , "mail"
        ) exclude(
        "com.sun.jdmk", "jmxtools"
        ) exclude(
        "com.sun.jmx", "jmxri"
        )
    )
  ) dependsOn (fluent)

}
