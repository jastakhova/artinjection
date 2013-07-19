package artinjection.crawler

/**
 * User: Julia Astakhova
 * Date: 5/7/13
 */
case class DumpSettings(directory: String, prefix: String)

object CommonProperties {

  val rootDirectory = "/tmp/artinjection/"

  val searchSettings = DumpSettings(rootDirectory + "search", "asked_for_")
  val pageSettings = DumpSettings(rootDirectory + "pages", "")

  val resultDirectory = rootDirectory + "result/"
}


