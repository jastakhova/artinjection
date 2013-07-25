package artinjection.crawler

import akka.actor._
import java.net.URLEncoder
import artinjection.core.IOUtils
import java.io.File

/**
 * @author Julia Astakhova
 */
object CrawlerActor {

  def main(args : Array[String]) {
    val system = ActorSystem("Crawler")
    system.actorOf(Props[CrawlerActor], "CrawlerActor")
  }
}

class CrawlerActor extends BulkRequestorActor[SearchQueryActor.Query] with IOUtils {

  import BulkSenderActor._
  import SearchQueryActor._

  val dataDumper = new DataDumper(CommonProperties.searchSettings)

  protected def createSenderActor: ActorRef =
    context.actorOf(Props(new SearchQueryActor with JustOneQuery), "QueryActor")

  protected def processMessage(t : Query, sender: ActorRef) = {
    val downloadedPage = download("https://www.google.com/search?q=%s".format(URLEncoder.encode(t.query, "utf8")))
    dataDumper.dump(t.query.replaceAll(" ", "_"), downloadedPage)
    true
  }
}

class DataDumper(settings: DumpSettings) extends IOUtils {
  val prefix = settings.prefix + "%s.html"

  def dump(name: String, input: String) {
    new File(settings.directory).mkdirs()
    toFile(input.replaceAll("\n", ""), new File(settings.directory + "/" + prefix format name))
  }
}
