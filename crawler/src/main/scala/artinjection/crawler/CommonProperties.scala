package artinjection.crawler

/**
 * User: Julia Astakhova
 * Date: 5/7/13
 */
case class DumpSettings(directory: String, prefix: String)

object CommonProperties {

  val searchSettings = DumpSettings("/tmp/artinjection/search", "asked_for_")
  val pageSettings = DumpSettings("/tmp/artinjection/pages", "")
}


