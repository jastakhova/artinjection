package artinjection.crawler

import akka.actor._
import java.net.URLEncoder

/**
 * @author Julia Astakhova
 */
trait BulkRequestorActor[T] extends Actor {

  import BulkSenderActor._

  override def preStart() {
    createSenderActor ! SendMe
  }

  protected def createSenderActor: ActorRef

  protected def processMessage(t : T)

  def receive = {
    case Message(t: T) => {
      processMessage(t)
      sender ! MessageProcessed
    }
    case AllMessagesSent => {
      context.system.shutdown()
    }
  }

}
