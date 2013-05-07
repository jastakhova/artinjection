package artinjetion.crawler.util

import org.xml.sax.InputSource

import scala.xml._
import parsing._
import java.io.StringReader

/**
 * User: Julia Astakhova
 * Date: 5/7/13
 */
class HTML5Parser extends NoBindingFactoryAdapter {

  override def loadXML(source : InputSource, _p: SAXParser) = {
    loadXML(source)
  }

  def loadXML(source : String): Node = {
    loadXML(new InputSource(new StringReader(source)))
  }

  def loadXML(source : InputSource): Node = {
    import nu.validator.htmlparser.{sax,common}
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)
    rootElem
  }
}
