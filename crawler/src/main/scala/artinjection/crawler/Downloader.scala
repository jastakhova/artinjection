package artinjection.crawler

import artinjection.core.IOUtils
import artinjection.core.StringUtils._

/**
 * @author Julia Astakhova
 */
object Downloader extends LinkParser with IOUtils {

  def main(args: Array[String]) = {
    val dumper = new CrawledDataDumper(CommonProperties.pageSettings)
    parse().foldLeft(List[String]())((res, el) => res ++ el._2)
      .foreach(url => dumper.dump(clean(url, """(http://)|(/{1}.*)|(www.)"""), download(url)))
  }
}
