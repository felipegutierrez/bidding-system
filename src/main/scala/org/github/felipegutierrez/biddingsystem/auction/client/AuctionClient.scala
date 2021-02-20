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
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

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

  var bidOffer: BidOffer = BidOffer("", 0, "")

  def receive = {
    case bidRequest@BidRequest(requestId, bid) =>
      log.info(s"received bid request: $bidRequest")
      // val content = """{"id": 10, "attributes" : { "a": "1", "b": "0" }}""".stripMargin
      // val content = bidRequest.bid.toJson.toString
      val content = bidRequest.bid.toJson.toString
        .replace("[[", "{")
        .replace("]]", "}")
        .replace("\",\"", "\": \"")
        .replace("[", "")
        .replace("]", "")
      // println(content)

      val latch = new CountDownLatch(bidders.size)

      val listResponseFuture: List[Future[HttpResponse]] = bidders
        .map(bidder =>
          HttpRequest( // create the request
            HttpMethods.POST,
            uri = Uri(bidder), // uri = Uri("http://localhost:8081"),
            entity = HttpEntity(ContentTypes.`application/json`, content)
          )
        )
        .map(httpRequest => http.singleRequest(httpRequest).pipeTo(self)) // send the request

      listResponseFuture.foreach { response =>
        Await.result(response, 3 seconds)
        response.onComplete {
          case Success(value) => latch.countDown // println(s"response success: $value")
          case Failure(exception) =>
            println(s"response failure: $exception")
            latch.countDown
        }
      }
      latch.await(3, TimeUnit.SECONDS)
      println("sending response now...")
      // sender() ! Some("a:750")
      sender() ! Some(bidOffer.content)
      bidOffer = BidOffer("", 0, "")
    case resp@HttpResponse(StatusCodes.OK, headers, entity, _) =>
      log.info(s"received HttpResponse OK(200): $resp")
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        println("Got response, body: " + body.utf8String)
        val newBidOffer = BidOfferConverter.getBidOffer(body.utf8String)
        if (bidOffer.bid == 0) {
          println("new")
          bidOffer = BidOffer(newBidOffer.id, newBidOffer.bid, newBidOffer.content.replace("$price$", newBidOffer.bid.toString))
        } else if (newBidOffer.bid > bidOffer.bid) {
          println("replace new")
          bidOffer = BidOffer(newBidOffer.id, newBidOffer.bid, newBidOffer.content.replace("$price$", newBidOffer.bid.toString))
        } else {
          println("none")
        }
      }
    case resp@HttpResponse(code, _, _, _) =>
      log.info(s"Request failed, response code: $code")
      resp.discardEntityBytes()
  }
}
