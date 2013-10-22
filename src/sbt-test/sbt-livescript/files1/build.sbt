import sbtlivescript.SbtLiveScriptPlugin._
import LiveScriptKeys._
import sbt._

liveScriptSettings

version := "0.1"

val checkFiles = Def.taskKey[Unit]("Check files")

val filesToCheck = Def.settingKey[Seq[File]]("Files to check")

filesToCheck := Seq(file("test2.js"), file("test.js"))

checkFiles := Def.task {
  val outDir: File = (outputDirectory in (Compile, livescript)).value
  val files: Seq[File] = filesToCheck.value
  val result = files.map((f: File) => outDir / f.toString).
    forall((f:File) => f.exists)
  if (!result) sys.error("filesToCheck failed!")
  ()
}.value

