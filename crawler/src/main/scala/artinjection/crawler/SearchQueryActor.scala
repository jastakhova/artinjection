package artinjection.crawler

import akka.actor._
import artinjection.core.IOUtils
import akka.pattern.ask
import akka.util._
import akka.util.duration._
import akka.dispatch.Future
import akka.actor.Status.Success
import akka.actor.FSM.Failure

/**
 * @author Julia Astakhova
 */

object SearchQueryActor {

  case object SendMeQueries

  case class Query(query: String)

  case object QueryProcessed

  case object AllQueriesSent
}

class SearchQueryActor extends Actor {
  this: RetrieveQueryStrategy =>

  import SearchQueryActor._

  implicit val executionContext = context.system.dispatchers.defaultGlobalDispatcher
  implicit val askTimeout = Timeout(10.seconds)

  def receive = {
    case SendMeQueries => {
      val requestor = sender
      val processQueryFutures = getQueries().map(requestor ? Query(_))
      Future.sequence(processQueryFutures).andThen {
        case Left(t) => {
          t.printStackTrace()
        }
        case Right(_) => {
          requestor ! AllQueriesSent
        }
      }
    }
    case QueryProcessed =>
  }
}

trait RetrieveQueryStrategy {

  val queryPattern = "100 greatest %s paintings"

  protected def retrieveDifferentQueryParts(): Seq[String]

  def getQueries(): Seq[String] = retrieveDifferentQueryParts().map(queryPattern.format(_))
}

trait QueriesFromConfig extends RetrieveQueryStrategy with IOUtils {

  val configName = "art_categories.txt"

  def retrieveDifferentQueryParts(): Seq[String] = getResourceAsList(configName) :+ ""
}

trait JustOneQuery extends RetrieveQueryStrategy {

  def retrieveDifferentQueryParts(): Seq[String] = Seq("")
}
