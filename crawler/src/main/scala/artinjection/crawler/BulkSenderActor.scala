package artinjection.crawler

import akka.actor._
import akka.pattern.ask
import akka.dispatch.Future
import akka.util.Timeout
import akka.util.duration._

/**
 * @author Julia Astakhova
 */
object BulkSenderActor {
  case object SendMe

  case class Message[T](t : T)

  case object MessageProcessed

  case object AllMessagesSent
}

trait BulkSenderActor[T] extends Actor {

  import BulkSenderActor._

  implicit val executionContext = context.system.dispatchers.defaultGlobalDispatcher
  implicit val askTimeout = Timeout(10.seconds)

  def receive = {
    case SendMe => {
      val requestor = sender
      val futures = retrieve().map(requestor ? Message(_))
      Future.sequence(futures).andThen {
        case Left(t) => {
          t.printStackTrace()
          requestor ! AllMessagesSent
        }
        case Right(_) => {
          requestor ! AllMessagesSent
        }
      }
    }
    case MessageProcessed =>
  }

  protected def retrieve(): Seq[T]
}
