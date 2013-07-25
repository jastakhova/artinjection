name := "crawler"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "0.3.2"

libraryDependencies += "nu.validator.htmlparser" % "htmlparser" % "1.2.1"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0"