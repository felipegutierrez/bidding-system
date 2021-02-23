package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.util.ByteString
import org.github.felipegutierrez.biddingsystem.auction.message.BidJsonProtocol
import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol._
import org.github.felipegutierrez.biddingsystem.auction.util.BidResponseConverter
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 *
 */
object AuctionClientActor {
  def props(bidders: List[String]) = {
    Props(new AuctionClientActor(bidders))
  }
}

/**
 * The Auction Client is an Actor that is created with a list of bidders. These bidders will be consulted when a bid request message is received.
 * So, if there is no bidders to consult, the Auction Client actor will always answer with a null winner bid.
 * The Auction Client receives [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidRequest]] which
 * are converted to JSON using [[https://doc.akka.io/japi/akka-http/current/akka/http/scaladsl/marshallers/sprayjson/SprayJsonSupport.html SprayJsonSupport]]
 * and sent to each of the bidders on the list {@code bidders}.
 * In case that a bidder of the {@code bidders} list is not available, the Auction Client will not fail the whole request process.
 * Instead, it will failure this single bidder request by considering it a null HTTP Response after 5 seconds.
 * The bids received from the {@code bidders} are converted to [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse]] also using the
 * [[https://doc.akka.io/japi/akka-http/current/akka/http/scaladsl/marshallers/sprayjson/SprayJsonSupport.html SprayJsonSupport]].
 * The winner bid is processed using [[https://doc.akka.io/docs/akka/current/stream/index.html Akka-stream]] operators
 * and the response is extracted and sent back as a simple String wrapped into a [[https://www.scala-lang.org/api/2.12.9/scala/Some.html scala Some]] value.
 *
 * @param bidders the list of bidders received from the [[org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer]].
 */
class AuctionClientActor(bidders: List[String])
  extends Actor with ActorLogging
    with BidJsonProtocol with SprayJsonSupport {

  import context.dispatcher

  implicit val system = context.system
  val http = Http(system)

  def receive = {
    case bidRequest@BidRequest(requestId, bid) =>
      log.info(s"received bid request: $bidRequest")
      val content = bidRequest.bid.toJson.toString

      val bidOfferList = bidders.map { bidder =>
        HttpRequest( // create the request
          HttpMethods.POST,
          uri = Uri(bidder),
          entity = HttpEntity(ContentTypes.`application/json`, content)
        )
      }
        .map { httpRequest =>
          val httpResponseFuture = http.singleRequest(httpRequest).pipeTo(self) // this creates the first Future[HttpResponse]
          Await.ready(httpResponseFuture, 5 seconds)
          httpResponseFuture.value.get.getOrElse(HttpResponse(StatusCodes.NotFound))
        }.filter(httpResponse => httpResponse.status == StatusCodes.OK)
        .map { httpResponse =>
          // println(s"response: $httpResponse")
          val bidOfferFuture = httpResponse.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
            log.info("Got response, body: " + body.utf8String)
            BidResponseConverter.getBidResponse(body.utf8String)
          }
          Await.ready(bidOfferFuture, 5 seconds)
          bidOfferFuture.value.get.getOrElse(BidResponse("", 0, ""))
        }
      // bidOfferList.foreach ( bidOffer => println(s"bidOffer: ${bidOffer.id}, ${bidOffer.bid}, ${bidOffer.content}"))
      val bidOfferWinner = Some(bidOfferList)
        .filter(_.nonEmpty)
        .map(_.maxBy(_.bid))
      log.info(s"winner: $bidOfferWinner")
      sender() ! Some(bidOfferWinner.getOrElse(BidResponse("", 0, "")).content)
    case response@HttpResponse(StatusCodes.OK, headers, entity, _) => log.info(s"received HttpResponse OK(200): $response")
    case response@HttpResponse(code, _, _, _) => log.info(s"Request failed, response code: $code")
    case message => log.warning(s"unknown message: $message")
  }
}
