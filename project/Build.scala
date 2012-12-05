import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "jdbcdslog-fork"
  val appVersion = "0.1-SNAPSHOT"
  val baseName = "jdbcdslog"
  val appOrganization = "jp.furyu.jdbcdslog_fork"

  lazy val root = Project("root", base = file("."))
    .aggregate(core, slf4j, fluent)

  lazy val core = Project("core", base = file("core")).settings(
    organization := appOrganization,
    version := appVersion,
    resolvers ++= Seq(
      "Fluent Maven2 Repository" at "http://fluentd.org/maven2"
    ),
    libraryDependencies ++= Seq(
      "org.fluentd" % "fluent-logger" % "0.2.4",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "net.databinder" %% "dispatch-json" % "0.8.5" % "test",
      "org.slf4j" % "slf4j-api" % "1.5.10" ,
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
      ),
      "junit" % "junit" % "4.9" % "test",
      "hsqldb" % "hsqldb" % "1.8.0.10" % "test",
      "com.novocode" % "junit-interface" % "0.10-M2" % "test"
    )
  )

  lazy val slf4j = Project("slf4j", base = file("slf4j")).settings(
    organization := appOrganization,
    version := appVersion,
    resolvers ++= Seq(
    ),
    libraryDependencies ++= Seq(
    )
  )

  lazy val fluent = Project("fluent", base = file("fluent")).settings(
    organization := appOrganization,
    version := appVersion,
    resolvers ++= Seq( 
      "Fluent Maven2 Repository" at "http://fluentd.org/maven2"
    ),
    libraryDependencies ++= Seq(
      "org.fluentd" % "fluent-logger" % "0.2.4",
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "net.databinder" %% "dispatch-json" % "0.8.5" % "test"
    )
  )
  

//  lazy val play2 = PlayProject("tracker-sample", path = file("tracker/sample"), mainLang = SCALA).dependsOn(core, slf4j)

}
