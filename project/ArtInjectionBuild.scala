package artinjection

import sbt._
import Keys._

object ArtInjectionBuild extends Build {
  lazy val root =  Project(
    id = "artinjection",
    base = file("."),
    aggregate = Seq(crawler)
  )

  lazy val crawler = ProjectRef(file("./crawler"), "crawler")
}
