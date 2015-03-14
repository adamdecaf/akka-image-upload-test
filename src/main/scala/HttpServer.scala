package images.social
import akka.actor.ActorSystem
import akka.http.Http
import akka.http.model.StatusCodes
import akka.stream.scaladsl.Sink
import akka.http.Http
import akka.http.server.{Directives, RoutingSettings, RoutingSetup, Route}
import akka.http.marshalling._
import akka.stream.{FlowMaterializer, ActorFlowMaterializer}

trait HttpServer extends Logging with WithConfig {
  def routes: Route
  def system: ActorSystem

  private[this] implicit val sys: ActorSystem = system

  import sys.dispatcher // needed for response marshalling

  val interface = config.getConfigString("http.server.interface")
  val port = config.getConfigInt("http.server.port")

  lazy val binding = {
    val fullRoutes = {
      import Directives._
      implicit val setup = RoutingSetup.apply
      routes ~ defaultRoutes ~ complete(StatusCodes.NotFound)
    }

    // TODO: Workaround due to https://github.com/akka/akka/issues/16972
    // val bound = Http().bindAndstartHandlingWith(fullRoutes, interface = interface, port = port)
    val httpSink = Sink.foreach[Http.IncomingConnection] { conn => conn.flow.join(fullRoutes).run() }
    val bound = Http().bind(interface = interface, port = port).to(httpSink).run()

    log.info(s"Bound to port ${port} for HTTP server.")
    bound
  }

  lazy val defaultRoutes = {
    import Directives._

    val ping =
      if (config.getConfigBoolean("http.server.include-ping-route")) {
        (get & path("ping")) { complete("PONG") }
      } else {
        reject
      }

    ping
  }

  def startHttpServer(): Unit = {
    log.info("Starting HTTP server.")
    binding
  }

  binding foreach { bound =>
    ShutdownHook(s"bringing down HTTP server on port ${port}.") {
      bound.unbind().onComplete(_ => println(s"HTTP server shut down."))
    }
  }
}
