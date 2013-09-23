/**
 *
 * sbt-livescript / LogiDev - [Fun Functional] / Logikujo.com
 *
 * 14/09/13 :: 16:29 :: eof
 *
 */
package sbtlivescript

import sbt._
import com.logikujo.sbt._
import npm._
import scalaz._
import scalaz.syntax.nel._
import scalaz.syntax.validation._
import scalaz.syntax.applicative._
import scalaz.syntax.std.option._
import net.liftweb.json._

object SbtLiveScriptPlugin extends Plugin {
  import sbt.Keys._
  import LiveScriptKeys._
  import Implicits._

  object LiveScriptKeys {
    val livescript = Def.taskKey[Seq[ValidationNel[String,File]]]("Compiles livescript files")
    //val init = Def.taskKey[File]("Initilize LiveScript environment")
    val outputDirectory = Def.settingKey[File]("Destination directory where to put compiled files")
    val liveScriptPackage = Def.settingKey[ValidationNel[String,LiveScript]]("Package to use to compile livescript files")
    val npmProgram = Def.settingKey[ValidationNel[String,Npm]]("npm executable program")
  }

  def liveScriptSettingsIn(conf: Configuration): Seq[Setting[_]] = inConfig(conf)(
    Seq(
      (sourceDirectory in livescript) := (sourceDirectory in conf).value / "livescript",
      (resourceManaged in livescript) := (resourceManaged in conf).value
    ) ++
    Seq(
      (outputDirectory in livescript) := (resourceManaged in livescript).value / "livescript",
      (liveScriptPackage in livescript) := (npmProgram in livescript).value.flatMap {
        (n: Npm) => n.getPackage[LiveScript]
      },
      (npmProgram in livescript) := Npm.get("npm"),
      //(init in livescript) := initTaskImpl.value,
      (clean in livescript) := cleanTaskImpl.value,
      (livescript in livescript) := compileTaskImpl.value
    )
  )

  val liveScriptSettings: Seq[Setting[_]] = liveScriptSettingsIn(Compile) ++ liveScriptSettingsIn(Test)

  lazy val cleanTaskImpl = Def.task {
    val log = streams.value.log
  }

  lazy val initTaskImpl = Def.task {
    file("KK")
  }

  lazy val compileTaskImpl = Def.task {
    val log = streams.value.log
    val lscript: ValidationNel[String, LiveScript] = (liveScriptPackage in livescript).value
    val lsSourceDir: File = (sourceDirectory in livescript).value
    val lsOutputDir: File = (outputDirectory in livescript).value
    log.info(s"Compiling livescript sources from `$lsSourceDir`")

    val outputFileName = (inputFile:File) => for {
      outFileName <- inputFile.relativeTo(lsSourceDir).toSuccess("relativeTo error.".wrapNel)
      outBaseName <- Option(outFileName.getParent).getOrElse("").successNel[String]
    } yield lsOutputDir / outBaseName / (inputFile.base + ".js")

    val k = for {
      inputFile <- (lsSourceDir ** "*.ls").get
    } yield for {
      outputFile <- outputFileName(inputFile)
      ls <- lscript
      writtenFile <- ls.compile(inputFile)(outputFile)
    } yield writtenFile
    k.foreach(println(_))
    k
  }
}

object Implicits extends RichProcessBuilderImplicits
