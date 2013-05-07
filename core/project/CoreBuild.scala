package core

import sbt._
import Keys._

object CoreBuild extends Build {
  lazy val root =  Project(
    id = "core",
    base = file(".")
  )
}
