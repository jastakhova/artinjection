package artinjetion.crawler

import java.io.File
import artinjetion.crawler.CommonProperties
import artinjetion.crawler.util.HTML5Parser

/**
 * User: Julia Astakhova
 * Date: 5/7/13
 */
object Parser extends LinkParser {
  def main(args: Array[String]) { retrieveLinks() }
}

trait LinkParser {
  def retrieveLinks(): Seq[String] = {
    new File(CommonProperties.crawledDataDirectory).listFiles()
      .filter(_.getName.startsWith(CommonProperties.crawledDataPrefix))
      .flatMap(f => parseLinks(io.Source.fromFile(f).mkString))
  }

  def parseLinks(body: String): Seq[String] = {
    new HTML5Parser().loadXML(body)
    Seq()
  }
}
