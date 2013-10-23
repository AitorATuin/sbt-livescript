import sbtlivescript.SbtLiveScriptPlugin._
import LiveScriptKeys._
import sbt._
import sbtclosure.SbtClosurePlugin._

liveScriptSettings

closureSettings

version := "0.1"

(outputDirectory in (Compile, livescript)) := (sourceDirectory in (Compile, ClosureKeys.closure)).value / "ls"

(ClosureKeys.closure in Compile) := ((ClosureKeys.closure in Compile) dependsOn (livescript in (Compile, livescript))).value

val checkFiles = Def.taskKey[Unit]("Check files")

filesToCheck := Seq(file("test2.js"), file("test.js"))

val filesToCheck = Def.settingKey[Seq[File]]("Files to check")

filesToCheck := Seq(file("test2.js"), file("test.js"))

checkFiles := Def.task {
  val log = streams.value.log
  val outDir: File = (outputDirectory in (Compile, livescript)).value
  val files: Seq[File] = filesToCheck.value
  val outputFiles = files.map((f: File) => outDir / f.toString)
  val result = outputFiles.forall((f:File) => f.exists)
  if (!result) sys.error("filesToCheck failed!")
  ()
}.value

val checkClosureWithLS = Def.taskKey[Unit]("Check sbt-closure with ls")

checkClosureWithLS := Def.task {
  val baseDir: File = baseDirectory.value
  val resourceDir: File = resourceManaged.value
  val fixture = sbt.IO.read(baseDir / "fixtures" / "script.js")
  val out = sbt.IO.read(resourceDir / "main" / "js" / "script.js")
  println(out)
  if (out.trim != fixture.trim) sys.error("Unexpected otut: \n\n" + out + "\n\n" + fixture)
  ()
}.value
