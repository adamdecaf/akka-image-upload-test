package images
import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer

package object social {
  lazy val ImagesActorSystem = ActorSystem("images-system")
  implicit lazy val Materializer = ActorFlowMaterializer()(ImagesActorSystem)
}
