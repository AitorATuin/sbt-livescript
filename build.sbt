sbtPlugin := true

organization := "com.logikujo"

name := "sbt-livescript"

//scalaVersion := "2.10.2"

version <<= sbtVersion(v =>
  if(v.startsWith("0.13")) "0.1.1"
  else error("unsupported sbt version %s" format v))

//libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1741"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.5" % "compile"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10" % "7.1.0-M2" % "compile"

seq(scriptedSettings:_*)

seq(lsSettings:_*)

scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-feature")

(LsKeys.tags in LsKeys.lsync) := Seq("sbt", "web", "livescript", "javascript")

(description in LsKeys.lsync) :=
  "sbt plugin to compile livescript files."


publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/eltimn/sbt-closure/raw/master/LICENSE")

homepage := Some(url("https://github.com/AitorATuin/sbt-livescript"))

pomExtra := (
  <scm>
      <url>git@github.com:AitorATuin/sbt-livescript.git</url>
      <connection>scm:git:git@github.com:AitorATuin/sbt-livescript.git</connection>
    </scm>
  <developers>
    <developer>
      <id>AitorATuin</id>
      <name>Aitor P. Iturri</name>
      <url>http://logikujo.com</url>
    </developer>
  </developers>
)
