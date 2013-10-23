sbt-livescript
==============

sbt (0.13) plugin supporting livescript files.

This sbt plugin allows to compile livescript files to javascript.

It uses npm (which is required to be installed) and the LiveScript package that comes with it. The LiveScript package will
be installed by the script, so the only requirement is to have npm installed on your system.

Yet in beta stage.

Installation
------------


Using with sbt-closure
----------------------

You can use *sbt-livescript* in conjuntion with [sbt-closure][], allowing you to use
the closure compiler with livescript source files and optionally to mix them with javascript ones.

In order to use it, first you must add the [sbt-closure][] plugin along with the sbt-livescript one as follows in
 the *plugins.sbt* file:

```
addSbtPlugin("com.logikujo" % "sbt-livescript" % "0.1")

addSbtPlugin("org.scala-sbt" % "sbt-closure" % "0.1.4")
```

The next step is to configure the sbt-livescript plugin to output javascript compiled files under a directory that
 can be reached by [sbt-closure][] and then let [sbt-closure][] closure task to run the livescript task:

```
import sbtlivescript.SbtLiveScriptPlugin._
import LiveScriptKeys._
import sbt._
import sbtclosure.SbtClosurePlugin._

liveScriptSettings

closureSettings

version := "0.1"

(outputDirectory in (Compile, livescript)) := (sourceDirectory in (Compile, ClosureKeys.closure)).value / "ls"

(ClosureKeys.closure in Compile) := ((ClosureKeys.closure in Compile) dependsOn (livescript in (Compile, livescript))).value
```


[sbt-closure]: https://github.com/eltimn/sbt-closure