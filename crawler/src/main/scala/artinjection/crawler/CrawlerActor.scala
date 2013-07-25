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

class CrawlerActor extends Actor with IOUtils {

  import SearchQueryActor._

  val dataDumper = new DataDumper(CommonProperties.searchSettings)

  override def preStart() {
    val queryActor = context.actorOf(Props(new SearchQueryActor with JustOneQuery), "QueryActor")
    queryActor ! SendMeQueries
  }

  def receive = {
    case Query(query) => {
      val downloadedPage = download("https://www.google.com/search?q=%s".format(URLEncoder.encode(query, "utf8")))
      dataDumper.dump(query.replaceAll(" ", "_"), downloadedPage)
      sender ! QueryProcessed
    }
    case AllQueriesSent => {
      context.system.shutdown()
    }
  }
}

class DataDumper(settings: DumpSettings) extends IOUtils {
  val prefix = settings.prefix + "%s.html"

  def dump(name: String, input: String) {
    new File(settings.directory).mkdirs()
    toFile(input.replaceAll("\n", ""), new File(settings.directory + "/" + prefix format name))
  }
}
