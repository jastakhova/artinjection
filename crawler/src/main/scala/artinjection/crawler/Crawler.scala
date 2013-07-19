package artinjection.crawler

import java.io._
import artinjection.core.IOUtils
import java.net.URLEncoder

object Crawler extends CrawlerStrategy with ThinQueryStrategy {
  def main(args : Array[String]) = crawl(
    /*UsualQueryStrategy("art_categories.txt")*/ this,
    "100 greatest %s paintings",
    new CrawledDataDumper(CommonProperties.searchSettings))
}

class CrawledDataDumper(settings: DumpSettings) extends IOUtils {
  val prefix = settings.prefix + "%s.html"

  def dump(name: String, input: String) {
    new File(settings.directory).mkdirs()
    toFile(input.replaceAll("\n", ""), new File(settings.directory + "/" + prefix format name))
  }
}

trait QueryStrategy {
  def queries: Seq[String]
}

trait ThinQueryStrategy extends QueryStrategy {
  def queries: Seq[String] = Seq("")
}

case class UsualQueryStrategy(querySource: String) extends QueryStrategy with IOUtils {
  def queries: Seq[String] = getResourceAsList(querySource) :+ ""
}

trait CrawlerStrategy extends IOUtils {
  def crawl(querySource: QueryStrategy, queryPattern: String, crawledDataDumper: CrawledDataDumper) {
    querySource.queries
      .map(query => (query, queryPattern.format(query)))
      .map {query =>
        (query._1, download("https://www.google.com/search?q=%s".format(URLEncoder.encode(query._2, "utf8"))))}
      .foreach {output => crawledDataDumper.dump(output._1, output._2)}
  }
}