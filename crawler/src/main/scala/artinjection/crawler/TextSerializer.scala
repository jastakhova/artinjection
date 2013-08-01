package artinjection.crawler

import artinjection.core.IOUtils
import java.io.File

/**
 * @author Julia Astakhova
 */
trait Serializer[K] {


}

class TextSerializer[T](name: String) extends IOUtils {

  var partsToSerialize = List[T]()

  def addPartToSerialize(partToSerialize: T) {
    partsToSerialize ::= partToSerialize
  }

  def done() {
    new File(CommonProperties.resultDirectory).mkdirs()
    toFile(partsToSerialize.mkString("\n"), CommonProperties.resultDirectory + name + ".txt")
  }
}

class Page(name: String, records: Seq[String]) {

  override def toString(): String = name + " : " + records.mkString("\n\t")
}
