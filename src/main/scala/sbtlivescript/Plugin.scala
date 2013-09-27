/**
 *
 * sbt-livescript / LogiDev - [Fun Functional] / Logikujo.com
 *
 * 14/09/13 :: 16:29 :: eof
 *
 */
package sbtlivescript

import scala.collection.mutable.{Map => MMap}
import sbt._
import com.logikujo.sbt._
import npm._
import scalaz._
import scalaz.syntax.nel._
import scalaz.syntax.monoid._
import scalaz.syntax.validation._
import scalaz.syntax.applicative._
import scalaz.syntax.std.option._
import scalaz.syntax.traverse._
import scalaz.std.list._
import scalaz.std.string._
import net.liftweb.json._

object SbtLiveScriptPlugin extends Plugin {
  import sbt.Keys._
  import LiveScriptKeys._
  import Implicits._

  private def getParent(file: File): String = Option(file.getParent).getOrElse("")
  private def getParent(s: String): String = getParent(file(s))

  object LiveScriptKeys {
    val livescript = Def.taskKey[ValidationNel[String,List[File]]]("Compiles livescript files")
    val outputDirectory = Def.settingKey[File]("Destination directory where to put compiled files")
    val liveScriptPackage = Def.settingKey[ValidationNel[String,LiveScript]]("Package to use to compile livescript files")
    val npmProgram = Def.settingKey[ValidationNel[String,Npm]]("npm executable program")
    val watchedFiles = Def.taskKey[Seq[File]]("Files to watch")
  }

  def liveScriptSettingsIn(conf: Configuration): Seq[Setting[_]] = inConfig(conf)(
    Seq(
      (outputDirectory in livescript) := (resourceManaged in livescript).value / "livescript",
      (liveScriptPackage in livescript) := (npmProgram in livescript).value.flatMap {
        (n: Npm) => n.getPackage[LiveScript]
      },
      (npmProgram in livescript) := Npm.get("npm"),
      (clean in livescript) := cleanTaskImpl.value,
      (livescript in livescript) := compileTaskImpl.value,
      (unmanagedSources in livescript) := liveScriptSourcesImpl.value
      //(watchSources in livescript) <<= (unmanagedSources in livescript)
    ) ++
    Seq(
      (sourceDirectory in livescript) := (sourceDirectory in conf).value / "livescript",
      (resourceManaged in livescript) := (resourceManaged in conf).value,
      (compile in conf) <<= (compile in conf) dependsOn (livescript in livescript),
      (clean in conf) <<= (clean in conf) dependsOn (clean in livescript)
    )
  ) ++ Seq(
    watchSources ++= (unmanagedSources in livescript in conf).value
  )

  val liveScriptSettings: Seq[Setting[_]] = liveScriptSettingsIn(Compile) ++ liveScriptSettingsIn(Test)

  object CleanTask {
    private val files: MMap[String, File] = MMap.empty
    def ++(xs:List[File]) = xs.foreach {f => files += (f.toString -> f)}
    def --(xs:List[File]) = xs.foreach {f => files -= f.toString }
    def clean: Unit = files.values.foreach { IO.delete(_)}
  }

  // TODO: Add timestamp to extension: ls_{timestamp}.js to avoid conflicts on removing files
  lazy val cleanTaskImpl = Def.task {
    val log = streams.value.log
    val lsOutputDir: File = (outputDirectory in livescript).value
    log.info("Cleaning livescript compiled files")
    (lsOutputDir ** "*.ls").get map {
      f => (f, file(getParent(f)) / (f.base + ".js"))
    } foreach {fp: (File,File) => IO.delete(fp._1 :: fp._2 :: Nil)}
  }

  lazy val liveScriptSourcesImpl = Def.task {
    val lsSourceDir: File = (sourceDirectory in livescript).value
    (lsSourceDir ** "*.ls").get
  }

  /*
   * For each compiled file there should be a file with extension .ls. Example:
   *    sourceDir/output.js
   *    sourceDir/output.ls <- original file from where the js file was compiled.
   */
  lazy val compileTaskImpl : Def.Initialize[Task[ValidationNel[String, List[File]]]] = Def.task {
    val log = streams.value.log
    val lscript: ValidationNel[String, LiveScript] = (liveScriptPackage in livescript).value
    val lsSourceDir: File = (sourceDirectory in livescript).value
    val lsOutputDir: File = (outputDirectory in livescript).value
    log.info(s"Compiling livescript sources from `$lsSourceDir`")

    val outputFileName = (inputFile:File) => for {
      outFileName <- inputFile.relativeTo(lsSourceDir).toSuccess("relativeTo error.".wrapNel)
      outBaseName <- Option(outFileName.getParent).getOrElse("").successNel[String]
    } yield {
      val f = lsOutputDir / outBaseName
      ((f / inputFile.name) -> (f / (inputFile.base + ".js")))
    }

    val outputFiles = (inputFile: File) => for {
      outFileName <- inputFile.relativeTo(lsSourceDir)
      outBaseName <- Option(getParent(outFileName))
      jsFile = lsOutputDir / outBaseName / (inputFile.base + ".js")
      touchFile = lsOutputDir / outBaseName / inputFile.name
      if inputFile newerThan jsFile
    } yield (touchFile -> jsFile)

    (for {
      inputFile <- (lsSourceDir ** "*.ls").get
    } yield for {
      files <- outputFileName(inputFile)
      (touchFile, outputFile) = files
      ls <- lscript
      writtenFile <- ls.compile(inputFile)(outputFile)
      _ = IO.touch(touchFile)
    } yield writtenFile).toList.sequenceU
  }
}

object Implicits extends RichProcessBuilderImplicits
