package artinjection.crawler

import akka.actor.{ActorSystem, Props, ActorRef}
import util.HTML5Parser
import artinjection.core.StringUtils._
import artinjection.crawler.util.HTML5Parser
import scala.xml._
import akka.dispatch.Future
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.routing.RandomRouter

/**
 * @author Julia Astakhova
 */
object LinkParserActor {
  case class Link(url: String)

  def main(args : Array[String]) {
    val system = ActorSystem("LinkParser")
    system.actorOf(Props[LinkParserActor], "LinkParserActor")
  }
}

class LinkParserActor extends BulkRequestorActor[FileLifterActor.LiftedFile] {

  import FileLifterActor._
  import BulkSenderActor._
  import LinkParserActor._

  val downloader = context.actorOf(Props[DownloaderActor].withRouter(RandomRouter(nrOfInstances = 10)), "DownloaderActor")

  implicit val executionContext = context.system.dispatchers.defaultGlobalDispatcher
  implicit val askTimeout = Timeout(60.seconds)

  protected def createSenderActor: ActorRef =
    context.actorOf(Props(new FileLifterActor(CommonProperties.searchSettings)), "FileLifterActor")

  protected def processMessage(file: LiftedFile, sender : ActorRef)  = {
    val requestor = sender
    val aElems = new HTML5Parser().loadXML(file.content) \\ "h3" \ "a"
    val hrefs = aElems flatMap(_.attribute("href"))
    val hrefValues = hrefs.foldLeft(Seq[String]())((res, hrefAttributes) => res ++ hrefAttributes.map(_.toString))
    val futures = hrefValues.map(clean(_, """(/url\?q=)|(&.*)""")).map(downloader ? Link(_))
    Future.sequence(futures).andThen {
      case Left(t) => {
        println("Message processing failed with downloading")
        t.printStackTrace()
        requestor ! MessageProcessed
      }
      case Right(_) => {
        requestor ! MessageProcessed
      }
    }
    false
  }

  override def receive = bulkReceive orElse {
    case MessageProcessed =>
  }

}
