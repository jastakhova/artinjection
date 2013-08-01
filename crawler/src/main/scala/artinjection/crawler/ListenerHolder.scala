package artinjection.crawler

import akka.actor._

/**
 * @author Julia Astakhova
 */
trait ListenerHolder {

  var listeners: Seq[ActorRef] = Seq.empty

  def addListener(listener: ActorRef) { listeners +:= listener}

  def sendToListeners[T](message: T) { listeners.foreach( _ ! message )}

}
