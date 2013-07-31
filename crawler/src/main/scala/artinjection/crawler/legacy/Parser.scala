package artinjection.crawler.legacy

import java.io.File
import artinjection.core.StringUtils._
import artinjection.crawler.util.HTML5Parser
import scala.xml._
import artinjection.core.IOUtils
import scala.Array
import artinjection.crawler.{CommonProperties, DumpSettings, Page, TextSerializer}

/**
 * User: Julia Astakhova
 * Date: 5/7/13
 */
object Parser extends PageParser {
  def main(args: Array[String]) {
    val serializer = new TextSerializer[Page]("page_results")
    parse().foreach(entry => serializer.addPartToSerialize(new Page(entry._1, entry._2)))
    serializer.done()
  }
}

trait CommonParser {
  def parse(): Map[String, Seq[String]] = {
    val settings = getSettings
    new File(settings.directory).listFiles()
      .filter(_.getName.startsWith(settings.prefix))
      .map(f => (f.getName -> parsePage(io.Source.fromFile(f).mkString, f.getName)))
      .foldLeft(Map[String, Seq[String]]())((res, entry) =>
         res + entry)
  }

  def parsePage(body: String, name: String): Seq[String]

  def getSettings: DumpSettings
}

class LinkParser extends CommonParser {

  def parsePage(body: String, name: String): Seq[String] = {
    val aElems = new HTML5Parser().loadXML(body) \\ "h3" \ "a"
    val hrefs = aElems flatMap(_.attribute("href"))
    val hrefValues = hrefs.foldLeft(Seq[String]())((res, hrefAttributes) => res ++ hrefAttributes.map(_.toString))
    hrefValues.map(clean(_, """(/url\?q=)|(&.*)"""))
  }

  def getSettings: DumpSettings = CommonProperties.searchSettings
}

class PageException(pageName: String, message: String) extends RuntimeException("consider page " + pageName + "\n" + message)
class TooManyAppropriateListsFoundException[T](pageName: String, lists: Seq[T])
  extends PageException(pageName, "found lists are: " + lists.mkString("\n\n\n"))
class SomeRecordsAreUntrustedException(pageName: String, allRecords: Seq[String], untrustedRecords: Seq[String])
  extends PageException(pageName, "all records are: " + allRecords.mkString("\n") + "\nuntrusted records are: " + untrustedRecords.mkString("\n"))

class PageInstanceParser(pageName: String) extends IOUtils {

  lazy val prepositionDictionary = getResourceAsList("preposition_dictionary.txt")

  case class HTMLListStructure(listElement: String, rowElement: String)

  def parse(body: String) = {
    val elems = Array(HTMLListStructure("ul", "li"), HTMLListStructure("ol", "li"), HTMLListStructure("tbody", "tr"))
    join(elems.flatMap(tryParsePageForAnElement(body, _))).getOrElse(Seq.empty)
  }

  private def tryParsePageForAnElement(body: String, structure: HTMLListStructure): Option[Seq[String]] =
    join((new HTML5Parser().loadXML(body) \\ structure.listElement).flatMap(retrieveList(_, structure)))

  private def join[T](seq: Seq[Seq[T]]): Option[Seq[T]] =
    if (seq.isEmpty)
      None
    else
      Some(seq.foldLeft(List[T]())((res, el) => res ++ el))

  private def justOne[T](seq: Seq[T]): Option[T] =
    seq.length match {
      case 0 => None
      case 1 => Some(seq.head)
      case _ => throw new TooManyAppropriateListsFoundException(pageName, seq)
    }

  private def retrieveList(listNode: Node, structure: HTMLListStructure): Option[Seq[String]] = {
    val records = (listNode \ structure.rowElement).map(_.text.replaceAll("\n", "").replaceAll("[\t ]+", " ").trim)
      .filter(_.matches(".*[A-Z].*"))
    if (records.isEmpty) return None
    val untrustedRecords = records.filter(! recordLooksLikePaintingReference(_))
    untrustedRecords.length match {
      case 0 if records.exists(recordHasYearPointer)                                                 => Some(records)
      case untrustedCount if untrustedCount == records.length || untrustedCount > records.length / 2 => {
        println("1 consider page " + pageName + " with " + "|||" + records.head + "||| total:" + records.length + " untrusted: " + untrustedCount)
        if ("historyofpainters.com.html" == pageName) println(untrustedRecords.mkString("\n"))
        None
      }
      case _                                                                                         =>
        records.filter(recordHasYearPointer).length match {
          case 0 => None
          case hasYearCount if hasYearCount > records.length / 2  => Some(records)
          case hasYearCount if hasYearCount < records.length / 10 => {
            println("2 consider page " + pageName + " with " + "|||" + records.head + "||| total:" + records.length + " has year: " + hasYearCount)
            None
          }
          case _                                                  =>
            throw new SomeRecordsAreUntrustedException(pageName, records, untrustedRecords)
        }
    }
  }

  private def recordHasYearPointer(record: String): Boolean =
    record.matches(".*((1[0-9]{3})|([0-9]{2}th)).*")

  private def recordLooksLikePaintingReference(record: String): Boolean = {
    val words = record.split("[\\p{Punct} &&[^']]").filter(!_.isEmpty)
    words.filter(_.matches("^[A-Z].*")).filter(word => !prepositionDictionary.contains(word.toLowerCase())).size > 2 &&
      words.forall(word => !word.matches("^[a-z].*") || prepositionDictionary.contains(word))
  }
}

trait PageParser extends CommonParser with IOUtils {

  def parsePage(body: String, name: String): Seq[String] = new PageInstanceParser(name).parse(body)

  def getSettings: DumpSettings = CommonProperties.pageSettings
}

