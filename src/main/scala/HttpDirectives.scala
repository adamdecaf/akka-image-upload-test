package images.social
import akka.http.server.{Directives, Directive1, PathMatchers}
import akka.http.model.headers.`User-Agent`
import akka.stream.ActorFlowMaterializer
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

trait HttpDirectives extends Directives with JsonMarshalling {
  def system: ActorSystem

  protected implicit lazy val execution: ExecutionContext = system.dispatcher

  type Route = akka.http.server.Route

  val PathUUID = PathMatchers.JavaUUID.map(_.toString)
  val StatusCodes = akka.http.model.StatusCodes

  protected def getIPAddress: Directive1[String] = extractClientIP.map(_.toOption.map(_.getHostAddress).getOrElse("Unknown"))
  protected def getUseragent: Directive1[String] = optionalHeaderValueByType[`User-Agent`]().map(_.map(_.value).getOrElse("Unknown"))
}
