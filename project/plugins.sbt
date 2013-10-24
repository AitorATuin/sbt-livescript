resolvers += Resolver.url("scalasbt", new URL(
  "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com"
)

libraryDependencies <+= sbtVersion { v =>
  "org.scala-sbt" % "scripted-plugin" % v
}

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")
