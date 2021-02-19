package org.github.felipegutierrez.biddingsystem.auction


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.duration._

object AuctionSystem {

  implicit val system = ActorSystem("AuctionSystem")
  implicit val defaultTimeout = Timeout(3 seconds)

  val routes: Route = {
    get {
      path(IntNumber) { (adId: Int) =>
        println(s"ad ID $adId received")
        complete(StatusCodes.OK)
      } ~ pathEndOrSingleSlash {
        complete(StatusCodes.BadRequest)
      } ~ {
        complete(StatusCodes.Forbidden)
      }
    }
  }

  def run() = {
    println("Action system started, listening on port 8080 and waiting parameters as described below")
    println("http://localhost:8080/[id]?[key=value,...]")
    println("")

    Http()
      .newServerAt("localhost", 8080)
      .bindFlow(routes)
  }

  class AuctionSystemException(message: String) extends RuntimeException(message)

}
