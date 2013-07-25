package artinjection.crawler

import akka.actor._
import java.io.File
import akka.pattern.ask
import akka.dispatch.Future

/**
 * @author Julia Astakhova
 */
object FileLifterActor {

  case class LiftedFile(content: String, name: String)
}

class FileLifterActor(settings: DumpSettings) extends BulkSenderActor[FileLifterActor.LiftedFile] {

  import FileLifterActor._

  override protected def timeout: Int = 60

  protected def retrieve(): Seq[LiftedFile] =
    new File(settings.directory).listFiles()
      .filter(_.getName.startsWith(settings.prefix))
      .map(f => LiftedFile(io.Source.fromFile(f).mkString, f.getName))
}
