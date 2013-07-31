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

  protected def processMessage(t : T, sender: ActorRef): Boolean

  protected def onAllMessagesSent() = {}

  def receive = bulkReceive

  protected def bulkReceive: Receive = {
    case Message(t: T) => {
      if (processMessage(t, sender))
        sender ! MessageProcessed
    }
    case AllMessagesSent => {
      onAllMessagesSent()
      context.system.shutdown()
    }
  }
}
