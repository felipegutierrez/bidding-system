package org.github.felipegutierrez.biddingsystem.auction

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import spray.json._

import scala.concurrent.duration._

object AuctionSystem extends BidJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem("AuctionSystem")
  implicit val defaultTimeout = Timeout(3 seconds)

  def getBid(adId: Int, params: Seq[(String, String)]): Bid = {
    Bid(adId, params)
  }

  val routes: Route = {
    get {
      (path(IntNumber) & parameterSeq) { (adId: Int, params: Seq[(String, String)]) =>
        // handling requests as: "http://localhost:8080/2?c=5&b=2", make sure to use the request between quotes
        println(s"The ad ID: $adId contains the parameters: ${params.map(paramString).mkString(", ")}")

        val bid = getBid(adId, params)
        println(s"bid request: ${bid.toJson.prettyPrint}")

        // redirect to the list of bidders

        complete(StatusCodes.OK)
      } ~ pathEndOrSingleSlash {
        complete(StatusCodes.BadRequest)
      } ~ {
        complete(StatusCodes.Forbidden)
      }
    }
  }

  /**
   * Extracting parameters from the URI
   *
   * @param param
   * @return
   */
  def paramString(param: (String, String)): String = s"""${param._1} = '${param._2}'"""

  def run() = {
    println("Action system started, listening on port 8080 and waiting parameters as described below")
    println("http GET 'localhost:8080/3'")
    println("http GET 'localhost:8080/3?b=5'")
    println("http GET 'localhost:8080/3?b=5&c=10'")
    println("http GET 'localhost:8080/3?b=5&c=10&d=19'")
    println("")

    Http()
      .newServerAt("localhost", 8080)
      .bindFlow(routes)
  }

  class AuctionSystemException(message: String) extends RuntimeException(message)

}
