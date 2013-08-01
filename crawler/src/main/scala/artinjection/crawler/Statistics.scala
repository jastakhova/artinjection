package artinjection.crawler

import akka.actor._
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author Julia Astakhova
 */
trait Statistics {
  override def toString(): String = throw new RuntimeException("Not implemented")
}

case class FoundResourceStatistics(name: String, foundCount: Int)

case class FoundResourcesStatistics(val foundCount: Int, val resources: Seq[FoundResourceStatistics]) extends Statistics {
  override def toString(): String =
    "\nFound resources:" + resources.map(resource => "\n\t" + resource.name + ": " + resource.foundCount).mkString
}

case class NotFoundResourcesStatistics(val notFoundCount: Int, val resources: Seq[String]) extends Statistics {
  override def toString(): String =
    "\nNot found resources:\t" + resources.map(resource => "\n\t" + resource).mkString
}

case class ElementStatistics(name: String, path: String,
                             paintingCount: Int, consideredRecordCount: Int, hadYearRecordCount: Int,
                             example: Seq[String])

case class ResourceStatistics(val name: String, val wasFound: Boolean, val elements: Seq[ElementStatistics]) extends Statistics {
  override def toString(): String = {
    val sortedElements = elements.toList.sortBy(elem =>
      (elem.name, elem.paintingCount, elem.consideredRecordCount, elem.hadYearRecordCount))
    "\nResource:\t" + name + "\t" + wasFound + "\n" + sortedElements.map(
      element => "\n\t"
        + List(element.name, element.path, element.paintingCount,
          element.consideredRecordCount, element.hadYearRecordCount).mkString("\t")
        + element.example.map("\n\t\t\t\t" + _).mkString
    ).mkString
  }
}