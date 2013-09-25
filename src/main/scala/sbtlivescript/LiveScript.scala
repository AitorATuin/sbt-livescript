package sbtlivescript

import com.logikujo.sbt._
import npm._
import scala.util.{Try, Failure, Success}
import sbt.{ProcessBuilder => _, Process => _, _}
import scala.sys.process.ProcessBuilder
import scalaz._
import scalaz.syntax.monad._
import scalaz.std.option._
import scalaz.syntax.validation._
import scalaz.syntax.std.option._
import scala.sys.process


/**
 *
 * sbt-livescript / LogiDev - [Fun Functional] / Logikujo.com
 *
 * sbtlivescript 16/09/13 :: 20:12 :: eof
 *
 */

sealed trait LiveScriptCommands {
}

sealed trait LiveScript extends NpmPackageBase {
  lazy val compiler: Option[File] = commands.get("lsc")

  def compile(inFile: File)(outFile:File) = compiler.
    map(_ + "-c" + "-p" + inFile.toString).
      some (c => c.??!!.map(o => (outFile,IO.write(outFile, o))._1)).
      none {s"Error when compiling livescript file `${inFile.toString}`".failNel
  }
}

object LiveScript {
  implicit object liveScriptPackage extends NpmPackage[LiveScript] {
    val packageName = "LiveScript"
    val commands = List("lsc")
    def newPackage(c: Map[String, File]) = new LiveScript {
      val commands = c
    }
  }
}
