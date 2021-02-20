package org.github.felipegutierrez.biddingsystem.auction.server

import akka.pattern.ask
import akka.actor.{ActorPath, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import org.github.felipegutierrez.biddingsystem.auction.client.AuctionClientActor
import org.github.felipegutierrez.biddingsystem.auction.protocol.BidProtocol._
import org.github.felipegutierrez.biddingsystem.auction.{Bid, BidJsonProtocol}

import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AuctionServer extends BidJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem("AuctionServerSystem")
  implicit val defaultTimeout = Timeout(3 seconds)
  import system.dispatcher

  // val auctionClientSystem = ActorSystem("AuctionClientSystem")
  val bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
  val auctionClientActor = system.actorOf(AuctionClientActor.props(bidders), "auctionClientActor")

  val routes: Route = {
    get {
      (path(IntNumber) & parameterSeq) { (adId: Int, params: Seq[(String, String)]) =>
        // handling requests as: "http://localhost:8080/2?c=5&b=2", make sure to use the request between quotes
        println(s"The ad ID: $adId contains the parameters: ${params.map(paramString).mkString(", ")}")

        val bid = getBid(adId, params.toList)
        // println(s"bid request: ${bid.toJson.toString}")
        // send bid request to the AuctionClientActor asynchronously
        val validResponseFuture: Option[Future[HttpResponse]] = {
          val actorResponse: Future[Option[String]] = (auctionClientActor ? BidRequest(UUID.randomUUID().toString, bid)).mapTo[Option[String]]
          Option(actorResponse.map(msg => HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,msg.getOrElse(""))
          )))
        }
        val entityFuture: Future[HttpResponse] = validResponseFuture.getOrElse(Future(HttpResponse(StatusCodes.BadRequest)))
        complete(entityFuture)
      } ~ pathEndOrSingleSlash {
        complete(StatusCodes.BadRequest)
      } ~ {
        complete(StatusCodes.Forbidden)
      }
    }
  }

  def getBid(adId: Int, attributes: Seq[(String, String)]): Bid = {
    Bid(adId, attributes.toList)
  }

  /**
   * Extracting parameters from the URI
   *
   * @param param
   * @return
   */
  def paramString(param: (String, String)): String = s"""${param._1} = '${param._2}'"""

  def toHttpEntity(payload: String) =
    HttpEntity(ContentTypes.`application/json`, payload)

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
