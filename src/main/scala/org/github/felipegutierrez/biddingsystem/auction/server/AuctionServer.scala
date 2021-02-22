package org.github.felipegutierrez.biddingsystem.auction.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import org.github.felipegutierrez.biddingsystem.auction.client.AuctionClientActor
import org.github.felipegutierrez.biddingsystem.auction.protocol.BidProtocol._
import org.github.felipegutierrez.biddingsystem.auction.{Bid, BidJsonProtocol}

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._

object AuctionServer {
  def apply(bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")): AuctionServer = {
    new AuctionServer(bidders)
  }
}

class AuctionServer(bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083"))
  extends BidJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem("AuctionServerSystem")
  implicit val defaultTimeout = Timeout(5 seconds)

  import system.dispatcher

  val auctionClientActor = system.actorOf(AuctionClientActor.props(bidders), "auctionClientActor")

  val routes: Route = {
    get {
      (path(IntNumber) & parameterSeq) { (adId: Int, params: Seq[(String, String)]) =>
        // handling requests as: "http://localhost:8080/2?c=5&b=2", make sure to use the request between quotes
        // println(s"The ad ID: $adId contains the parameters: ${params.map(paramString).mkString(", ")}")

        val bid = getBid(adId, params.toList)
        // println(s"bid request: ${bid.toJson.toString}")
        // send bid request to the AuctionClientActor asynchronously
        val validResponseFuture: Option[Future[HttpResponse]] = {
          val actorResponse: Future[Option[String]] = (auctionClientActor ? BidRequest(UUID.randomUUID().toString, bid)).mapTo[Option[String]]
          Option(actorResponse.map(msg => HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg.getOrElse(""))
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

  def run() = {
    println("Action system started, listening on port 8080 and waiting parameters as described below")
    println("curl -s \"http://localhost:8080/1?a=5\"")
    println("curl -s \"http://localhost:8080/2?c=5&b=2\"")
    println("")

    Http()
      .newServerAt("localhost", 8080)
      .bindFlow(routes)
  }

  class AuctionSystemException(message: String) extends RuntimeException(message)

}
