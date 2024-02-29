package com.ebctech.web.control


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import com.ebctech.web.control.routes.TonsaRoutes
import com.ebctech.web.control.actor.TonsaRegistry
import akka.http.scaladsl.server.Directives._
import scala.util.{Failure, Success}

object Boot {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val host = system.settings.config.getString("taps.host")
    val port = system.settings.config.getInt("taps.port")

    val futureBinding = Http().newServerAt(host, port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val tonsaRegistryActor = context.spawn(TonsaRegistry(context.system), "TonsaRegistryActor")

      context.watch(tonsaRegistryActor)

      val tonsaRoutes = new TonsaRoutes(tonsaRegistryActor)(context.system)

      startHttpServer(concat(tonsaRoutes.tonsaRoutes))(context.system)

      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "TapsHttpServer")
  }
}
