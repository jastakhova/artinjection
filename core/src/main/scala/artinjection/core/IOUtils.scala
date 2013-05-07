package artinjection.core

import dispatch._
import java.io._
import scala.io._

/**
 * User: Julia Astakhova
 * Date: 5/6/13
 */
trait IOUtils {

  def toFile(is: InputStream, f: File) {
    val in = Source.fromInputStream(is)
    val out = new PrintWriter(f)
    try { in.getLines().foreach(out.print(_)) }
    finally {
      out.close
      is.close
    }
  }

  def toFile(is: String, f: File) {
    val out = new PrintWriter(f)
    try { is.split("\n").foreach(out.print(_)) }
    finally {
      out.close
    }
  }

  def download(theUrl: String): String =
    try {
      Thread.sleep(2000)
      Http(url(theUrl) <:< Map("User-Agent" -> "Firefox/3.0.15") OK as.String)()
    } catch {
      case e: Exception => ""
    }
}
