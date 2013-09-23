sbtPlugin := true

organization := "com.logikujo"

name := "sbt-livescript"

//scalaVersion := "2.10.2"

version <<= sbtVersion(v =>
  if(v.startsWith("0.13")) "0.1"
  else error("unsupported sbt version %s" format v))

//libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1741"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.5" % "compile"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10" % "7.1.0-M2" % "compile"

seq(scriptedSettings:_*)

scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-feature")
