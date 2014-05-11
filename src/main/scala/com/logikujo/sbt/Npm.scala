package com.logikujo.sbt
package npm

import scala.util.{Try, Failure, Success}
import sbt.{ProcessBuilder => _, Process => _,  _}
import scala.sys.process._
import scala.language.implicitConversions
import net.liftweb.json._
import scalaz._

import scalaz.syntax.nel._
import scalaz.Validation._
import scalaz.syntax.validation._
import scalaz.syntax.std.option._
import scalaz.syntax.traverse._
//import scalaz.syntax.applicative._
import scalaz.syntax.std.boolean._
//import scalaz.syntax.monad._
import scalaz.syntax.applicative._

import scalaz.std.list._

/**
 *
 * sbt-livescript / LogiDev - [Fun Functional] / Logikujo.com
 *
 * sbtlivescript 14/09/13 :: 17:55 :: eof
 *
 */

trait NpmPackageBase {
  val commands: Map[String, File]
}

object NpmPackageBase {
}

trait NpmPackage[P] {
  val packageName: String
  val commands: List[String]
  def newPackage(c: Map[String, File]): P
}

sealed trait NpmCommands {
  val npm: List[String] => ProcessBuilder

  lazy val root: ProcessBuilder  = npm (List("root"))
  lazy val bin: ProcessBuilder = npm (List("bin"))
  lazy val list: ProcessBuilder = npm (List("list", "--json"))
  lazy val has_pkg: String => ProcessBuilder = pkg => npm(List("list")) #| s"grep $pkg"
  lazy val info: String => ProcessBuilder = pkg => npm(List("info", pkg, "--json"))
  lazy val install: String => ProcessBuilder = pkg => npm(List("install", pkg))
}

sealed trait Npm extends NpmCommands {
  import Npm._
  lazy val rootDir: ValidationNel[String, File] = root.??!!.map(s => file(s.trim))
  lazy val binDir: ValidationNel[String, File] = root.??!!.map(s => file(s.trim))
  def hasPackage(pkg: String) = (for {
    output <- list.??!!
    json <- fromTryCatch(parse(output)).map( _ \ "dependencies" \ pkg)
  } yield json != JNothing) | false

  def binaryForPackage[P](binName: String)(implicit pkg: NpmPackage[P]): ValidationNel[String, File] = {
    def binaryFile = (binName: String) => for {
      output <- info(pkg.packageName).??!!
      json <- fromTryCatch(parse(output)).leftMap(_.toString).toValidationNel
      bin <- (json \ "bin" \ binName).find(c => c != JNothing).
        toSuccess(s"Unable to get binary `$binName` for package `$pkg`".wrapNel)
    } yield file(bin.values.toString)

    hasPackage(pkg.packageName) ?
      binaryFile(binName) |
      "Package `${pkg.packageName}` is not installed.".failNel[File]
  }

  def getPackage[P](implicit pkg: NpmPackage[P]): ValidationNel[String, P] = {
    val installPackage = (pkg: String) => install(pkg).??!!

    lazy val getPackage = pkg.commands.traverseU {
     (c: String) => (binDir |@| binaryForPackage[P](c)) {
        (a: File,b:File) => c -> a / pkg.packageName / b.toString
      }
    } map (_.toMap) map (pkg.newPackage)

    hasPackage(pkg.packageName) ?
      getPackage |
      (installPackage(pkg.packageName) flatMap (_ => getPackage))
  }
}

object Npm {
  type NpmResult[A] = ValidationNel[String, A]
  type NpmF[A] = Npm => A
  private def existsCommand(pb: ProcessBuilder) = pb.?!

  def apply(s: String): Npm = new Npm {
    val npm: List[String] => ProcessBuilder = args => Process(Seq(s) ++ args)
  }
  def get(cmd: String):ValidationNel[String, Npm] = get(cmd, cmd)
  def get(cmd: String, testCmd: String): ValidationNel[String, Npm] = testCmd.??!!.map(_ => apply(cmd))
  //existsCommand(s).map(_ => apply(s))
}

object NpmF {
  def apply[A](a: => A): Npm => A = (_: Npm) => a
  def apply[A](f: Npm => A): Npm => A = f
}
