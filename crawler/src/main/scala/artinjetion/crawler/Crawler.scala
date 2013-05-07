package artinjetion.crawler

import java.io._
import artinjection.core.IOUtils
import java.net.URLEncoder

object Crawler extends CrawlerStrategy with CrawledData with ThinQueryStrategy {
  def main(args : Array[String]) = crawl(/*UsualQueryStrategy("art_categories.txt")*/ this, "100 greatest %s paintings", this)
}

trait CrawledData extends IOUtils {
  val destinationDir = CommonProperties.crawledDataDirectory
  val prefix = CommonProperties.crawledDataPrefix + "%s.html"

  def process(name: String, input: String) {
    new File(destinationDir).mkdir()
    toFile(input, new File(destinationDir + "/" + prefix format name))
  }
}

trait QueryStrategy {
  def queries: Seq[String]
}

trait ThinQueryStrategy extends QueryStrategy {
  def queries: Seq[String] = Seq("")
}

case class UsualQueryStrategy(querySource: String) extends QueryStrategy {
  def queries: Seq[String] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(querySource)).mkString
    val queries = text.split("\n").filterNot(_.isEmpty).filterNot(_.startsWith("#"))
    queries :+ ""
  }
}

trait CrawlerStrategy extends IOUtils {
  def crawl(querySource: QueryStrategy, queryPattern: String, crawledData: CrawledData) {
    querySource.queries
      .map(query => (query, queryPattern.format(query)))
      .map {query =>
        (query._1, download("https://www.google.com/search?q=%s".format(URLEncoder.encode(query._2, "utf8"))))}
      .foreach {output => crawledData.process(output._1, output._2)}
  }
}