package artinjection.crawler

import akka.actor.{ActorSystem, Props, ActorRef}
import artinjection.crawler.FileLifterActor.LiftedFile
import artinjection.core.IOUtils
import legacy.{PageInstanceParser, SomeRecordsAreUntrustedException, TooManyAppropriateListsFoundException}
import util.HTML5Parser
import xml.Node
import artinjection.crawler.legacy

/**
 * @author Julia Astakhova
 */
object PageParserActor {
  def main(args : Array[String]) {
    val system = ActorSystem("PageParser")
    system.actorOf(Props[PageParserActor], "PageParserActor")
  }
}

class PageParserActor extends BulkRequestorActor[FileLifterActor.LiftedFile] {

  val resultSerializer = new TextSerializer[Page]("page_results")
  val statistics = new StatisticsAccumulator

  protected def createSenderActor: ActorRef =
    context.actorOf(Props(new FileLifterActor(CommonProperties.pageSettings)), "FileLifterActor")

  protected def processMessage(file: LiftedFile, sender : ActorRef) = {
    val paintingsOrErrors = new PageInstanceParser(file.name, statistics).parse(file.content)
    paintingsOrErrors match {
      case Left(paintings) if !paintings.isEmpty => resultSerializer.addPartToSerialize(new Page(file.name, paintings))
      case Right(errors)   => errors.foreach(_.printStackTrace())
      case _               =>
    }
    true
  }

  override protected def onAllMessagesSent() {
    List(resultSerializer, statistics.createSerializer()).foreach(_.done())
  }
}

class StatisticsAccumulator {
  var foundStatistics: FoundResourcesStatistics = new FoundResourcesStatistics(0, Seq.empty)
  var notfoundStatistics: NotFoundResourcesStatistics = new NotFoundResourcesStatistics(0, Seq.empty)
  var resources: Map[String, ResourceStatistics] = Map.empty

  def getResource(name: String): ResourceStatistics = resources.getOrElse(name, new ResourceStatistics(name, false, Seq.empty))

  def createSerializer() = {
    (List(foundStatistics, notfoundStatistics) ++ resources.values).foldLeft(
      new TextSerializer[Statistics]("statistics"))((serializer, statistics) => {
        serializer.addPartToSerialize(statistics)
        serializer
    })
  }
}

class PageException(pageName: String, message: String) extends RuntimeException("consider page " + pageName + "\n" + message)
class TooManyAppropriateListsFoundException[T](pageName: String, lists: Seq[T])
  extends legacy.PageException(pageName, "found lists are: " + lists.mkString("\n\n\n"))
class SomeRecordsAreUntrustedException(pageName: String, allRecords: Seq[String], untrustedRecords: Seq[String])
  extends legacy.PageException(pageName, "all records are: " + allRecords.mkString("\n") + "\nuntrusted records are: " + untrustedRecords.mkString("\n"))

class PageInstanceParser(pageName: String, statistics: StatisticsAccumulator) extends IOUtils {

  lazy val prepositionDictionary = getResourceAsList("preposition_dictionary.txt")

  case class HTMLListStructure(listElement: String, rowElement: String)

  def parse(body: String): Either[Seq[String], Seq[Throwable]] = {
    val elems = Array(HTMLListStructure("ul", "li"), HTMLListStructure("ol", "li"), HTMLListStructure("tbody", "tr"))
    val pairOf2ListsOfResultsAndErrors = elems.map(tryParsePageForAnElement(body, _))
      .foldLeft((Seq[String](), Seq[Throwable]()))((resultLists, elementEither) => elementEither match {
      case Left(stringSeq) => (resultLists._1 ++ stringSeq, resultLists._2)
      case Right(error: Throwable)    => (resultLists._1, error +: resultLists._2)
    })
    if (pairOf2ListsOfResultsAndErrors._2.isEmpty) {
      if (!pairOf2ListsOfResultsAndErrors._1.isEmpty) {
        statistics.foundStatistics = new FoundResourcesStatistics(
          statistics.foundStatistics.foundCount + 1,
          new FoundResourceStatistics(pageName, pairOf2ListsOfResultsAndErrors._1.length) +: statistics.foundStatistics.resources)
        statistics.resources += pageName -> statistics.getResource(pageName).copy(wasFound = true)
      } else
        statistics.notfoundStatistics = new NotFoundResourcesStatistics(
          statistics.notfoundStatistics.notFoundCount + 1, pageName +: statistics.notfoundStatistics.resources)
      Left(pairOf2ListsOfResultsAndErrors._1)
    } else
      Right(pairOf2ListsOfResultsAndErrors._2)
  }

  private def tryParsePageForAnElement(body: String, structure: HTMLListStructure): Either[Seq[String], Seq[Throwable]] = {
    val listElements = new HTML5Parser().loadXML(body) \\ structure.listElement
    val pairOf2ListsOfResultsAndErrors: Pair[Seq[String], Seq[Throwable]] = listElements.map(retrieveList(_, structure))
      .foldLeft((Seq[String](), Seq[Throwable]()))((resultLists, elementEither) => elementEither match {
        case Left(Some(stringSeq)) => (resultLists._1 ++ stringSeq, resultLists._2)
        case Right(error)          => (resultLists._1, error +: resultLists._2)
        case _                     => resultLists
    })
    if (pairOf2ListsOfResultsAndErrors._2.isEmpty)
      Left(pairOf2ListsOfResultsAndErrors._1)
    else
      Right(pairOf2ListsOfResultsAndErrors._2)
  }

  private def join[T](seq: Seq[Seq[T]]): Option[Seq[T]] =
    if (seq.isEmpty)
      None
    else
      Some(seq.foldLeft(List[T]())((res, el) => res ++ el))

  private def justOne[T](seq: Seq[T]): Either[Option[T], Throwable] =
    seq.length match {
      case 0 => Left(None)
      case 1 => Left(Some(seq.head))
      case _ => Right(new TooManyAppropriateListsFoundException(pageName, seq))
    }

  private def addStat(stat: ElementStatistics) = {
    val preexistedElements = statistics.getResource(pageName)
    statistics.resources += pageName -> statistics.getResource(pageName).copy(elements = stat +: preexistedElements.elements)
  }

  private def retrieveList(listNode: Node, structure: HTMLListStructure): Either[Option[Seq[String]], Throwable] = {
    val records = (listNode \ structure.rowElement).map(_.text.replaceAll("\n", "").replaceAll("[\t ]+", " ").trim)
      .filter(_.matches(".*[A-Z].*"))
    if (records.isEmpty) return Left(None)
    val untrustedRecords = records.filter(! recordLooksLikePaintingReference(_))
    val hadYearCount = records.filter(recordHasYearPointer).length

    def addThisStat(foundCount: Int) =
      { addStat(new ElementStatistics(listNode.label, "", foundCount, records.length, hadYearCount, records.toList.take(3))) }

    val result = untrustedRecords.length match {
      case 0 if hadYearCount > 0  =>
        Left(Some(records))
      case untrustedCount if untrustedCount == records.length || untrustedCount > records.length / 2 => {
//        println("1 consider page " + pageName + " with " + "|||" + records.head + "||| total:" + records.length + " untrusted: " + untrustedCount)
        Left(None)
      }
      case _                                                                                         =>
        hadYearCount match {
          case 0 =>Left(None)
          case hasYearCount if hasYearCount > records.length / 2  => Left(Some(records))
          case hasYearCount if hasYearCount < records.length / 10 => {
//            println("2 consider page " + pageName + " with " + "|||" + records.head + "||| total:" + records.length + " has year: " + hasYearCount)
            Left(None)
          }
          case _                                                  =>
            Right(new SomeRecordsAreUntrustedException(pageName, records, untrustedRecords))
        }
    }

    result match {
      case Left(Some(records)) => addThisStat(records.length)
      case Left(None)          => addThisStat(0)
      case _                   =>
    }

    result
  }

  private def recordHasYearPointer(record: String): Boolean =
    record.matches(".*((1[0-9]{3})|([0-9]{2}th)).*")

  private def recordLooksLikePaintingReference(record: String): Boolean = {
    val words = record.split("[\\p{Punct} &&[^']]").filter(!_.isEmpty)
    words.filter(_.matches("^[A-Z].*")).filter(word => !prepositionDictionary.contains(word.toLowerCase())).size > 2 &&
      words.forall(word => !word.matches("^[a-z].*") || prepositionDictionary.contains(word))
  }
}
