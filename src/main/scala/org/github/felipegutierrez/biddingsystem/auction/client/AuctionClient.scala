package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.util.ByteString
import org.github.felipegutierrez.biddingsystem.auction.protocol.BidProtocol._
import org.github.felipegutierrez.biddingsystem.auction.{Bid, BidJsonProtocol, BidOffer, BidOfferConverter}
import spray.json._

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object AuctionClient {
  //  def main(args: Array[String]): Unit = {
  //    run()
  //  }

  def run() = {
    val system = ActorSystem("AuctionClientSystem")
    val auctionClientActor = system.actorOf(Props[AuctionClientActor], "auctionClientActor")
    auctionClientActor ! BidRequest(UUID.randomUUID().toString, Bid(2, List(("c", "5"), ("b", "2"))))
  }
}

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
          println(s"response: $httpResponse")
          val bidOfferFuture = httpResponse.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
            println("Got response, body: " + body.utf8String)
            BidOfferConverter.getBidOffer(body.utf8String)
          }
          Await.ready(bidOfferFuture, 5 seconds)
          bidOfferFuture.value.get.getOrElse(BidOffer("", 0, ""))
        }
      bidOfferList.foreach { bidOffer =>
        println(s"bidOffer: ${bidOffer.id}, ${bidOffer.bid}, ${bidOffer.content}")
      }
      val bidOfferWinner = Some(bidOfferList)
        .filter(_.nonEmpty)
        .map(_.maxBy(_.bid))
      // val bidOfferWinner = bidOfferList.maxBy(_.bid)
      // println(s"winner: $bidOfferWinner")
      sender() ! Some(bidOfferWinner.getOrElse(BidOffer("", 0, "")).content)
  }
}
