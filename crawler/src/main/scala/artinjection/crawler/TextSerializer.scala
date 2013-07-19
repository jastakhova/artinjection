package artinjection.crawler

import artinjection.core.IOUtils
import java.io.File

/**
 * @author Julia Astakhova
 */
trait Serializer[K] {


}

class TextSerializer[T <: Container](name: String) extends IOUtils {

  var partsToSerialize = List[T]()

  def addPartToSerialize(partToSerialize: T) {
    partsToSerialize = partToSerialize :: partsToSerialize
  }

  def done() {
    new File(CommonProperties.resultDirectory).mkdirs()
    toFile(partsToSerialize.filter(! _.isEmpty).mkString("\n"), CommonProperties.resultDirectory + name + ".txt")
  }
}

trait Container {
  def isEmpty: Boolean
}

class Page(name: String, records: Seq[String]) extends Container {

  def isEmpty: Boolean = records.isEmpty

  override def toString(): String = name + " : " + records.mkString("\n\t")
}
