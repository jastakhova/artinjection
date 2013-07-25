package artinjection.crawler

import akka.actor._
import artinjection.core.IOUtils
import akka.pattern.ask
import akka.util._

import akka.dispatch.Future
import akka.actor.Status.Success
import akka.actor.FSM.Failure

object SearchQueryActor {
  case class Query(query: String)
}


/**
 * @author Julia Astakhova
 */
class SearchQueryActor extends BulkSenderActor[SearchQueryActor.Query] {
  this: RetrieveQueryStrategy =>

  import SearchQueryActor._

  def retrieve(): Seq[Query] = getQueries().map(Query(_))
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
