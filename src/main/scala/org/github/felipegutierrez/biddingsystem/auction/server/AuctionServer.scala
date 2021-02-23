package org.github.felipegutierrez.biddingsystem.auction.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import org.github.felipegutierrez.biddingsystem.auction.client.AuctionClientActor
import org.github.felipegutierrez.biddingsystem.auction.message.Bid
import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol._

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._

/**
 *
 */
object AuctionServer {
  def apply(bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")): AuctionServer = {
    new AuctionServer(bidders)
  }
}

/**
 * The Auction Server is the first object started by the Bidding System.
 * It receives a list of bidders in its constructor that are assigned to the auction.
 * The bidders are passed to the auction client actor that is responsible to consult the bid.
 * The main purpose of the Auction Server is to have HTTP routes to receive requests as HTTP POST messages.
 * Other HTTP messages are not allowed.
 * The Auction Server receives responses from the Auction Client and send back a HttpResponse in the format of plain text.
 *
 * The flow is basically:
 *
 * 1 - the Auction Server converts request messages into [[org.github.felipegutierrez.biddingsystem.auction.message.Bid]] objects.
 * 2 - it uses the [[https://doc.akka.io/docs/akka/current/actors.html#ask-send-and-receive-future akka ask pattern]] to send the bid requests to the bidders.
 * 3 - it receives back the winner bid in a Future.
 * 4 - it sends the HttpResponse to the http client.
 *
 * @param bidders the list of bidders. If no bidders are passed as argument, the Auction Server will use a default list of bidders.
 */
class AuctionServer(bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")) {

  implicit val system = ActorSystem("AuctionServerSystem")
  implicit val defaultTimeout = Timeout(5 seconds)

  import system.dispatcher

  val auctionClientActor = system.actorOf(AuctionClientActor.props(bidders), "auctionClientActor")

  val routes: Route = {
    get {
      (path(IntNumber) & parameterSeq) { (adId: Int, params: Seq[(String, String)]) =>
        // handling requests as: "http://localhost:8080/2?c=5&b=2", make sure to use the request between quotes
        // println(s"The ad ID: $adId contains the parameters: ${params.map(paramString).mkString(", ")}")

        val bid = Bid(adId, params.toMap)
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

  /**
   * The method to start the Auction Server and listen on the host 0.0.0.0:8080
   *
   * @return
   */
  def run() = {
    println("Action system started, listening on port 8080 and waiting parameters as described below")
    println("curl -s \"http://127.0.0.1:8080/1?a=5\"")
    println("curl -s \"http://127.0.0.1:8080/2?c=5&b=2\"")
    println("")

    Http().newServerAt("0.0.0.0", 8080).bindFlow(routes)
  }
}
