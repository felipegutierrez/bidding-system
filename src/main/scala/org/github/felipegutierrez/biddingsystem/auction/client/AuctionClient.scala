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

object AuctionClientActor {
  def props(bidders: List[String]) = {
    Props(new AuctionClientActor(bidders))
  }
}

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
        .replace("[[", "{")
        .replace("]]", "}")
        .replace("\",\"", "\": \"")
        .replace("[", "")
        .replace("]", "")

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
  }
}
