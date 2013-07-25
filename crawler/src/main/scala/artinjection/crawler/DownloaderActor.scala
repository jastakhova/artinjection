package artinjection.crawler

import akka.actor._
import legacy.CrawledDataDumper
import artinjection.core.StringUtils._
import artinjection.core.IOUtils

/**
 * @author Julia Astakhova
 */
class DownloaderActor extends Actor with IOUtils {

  import LinkParserActor._
  import BulkSenderActor._

  val dumper = new CrawledDataDumper(CommonProperties.pageSettings)

  def receive = {
    case Link(url) => {
      println("link download")
      dumper.dump(clean(url, """(http://)|(/{1}.*)|(www.)"""), download(url))
      sender ! MessageProcessed
    }
  }
}
