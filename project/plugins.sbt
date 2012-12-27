import sbt.Defaults._

// sbt 0.11.2や0.11.3向けは https://github.com/typesafehub/sbtscalariform
// 0.12.0は https://github.com/sbt/sbt-scalariform
addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.5.1")

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

// 0.11.2や0.11.3向けはsbt-idea 1.0.0
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
    "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"
)

//addSbtPlugin("play" % "sbt-plugin" % "2.0.4")

resolvers += Resolver.url(
  "sbt-plugin-releases",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

libraryDependencies += sbtPluginExtra("reaktor" % "sbt-scct" % "0.2-SNAPSHOT", "0.11.2", "2.9.1")
