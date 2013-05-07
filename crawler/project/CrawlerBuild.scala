package crawler

import sbt._
import Keys._

object CrawlerBuild extends Build {
  lazy val root = Project(
    id = "crawler",
    base = file(".")
  ).dependsOn(core)

  lazy val core = ProjectRef(file("../core"), "core")
}
