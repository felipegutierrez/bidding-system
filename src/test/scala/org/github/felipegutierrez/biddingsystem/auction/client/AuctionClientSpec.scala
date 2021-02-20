package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpProtocols, HttpResponse, StatusCodes}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.github.felipegutierrez.biddingsystem.auction.Bid
import org.github.felipegutierrez.biddingsystem.auction.protocol.BidProtocol.BidRequest
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class AuctionClientSpec extends TestKit(ActorSystem("AuctionClientSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "the auction client actor" should {
    val bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
    val auctionClientActor = system.actorOf(AuctionClientActor.props(bidders), "auctionClientActorSpec")
    "receive bid requests" in {
      val bidRequestMsg = BidRequest(UUID.randomUUID().toString, Bid(1, List(("b", "5"), ("c", "10"))))
      EventFilter.info(message = s"received bid request: $bidRequestMsg", occurrences = 1) intercept {
        auctionClientActor ! bidRequestMsg
      }
    }
    "receive http response messages with 200 OK" in {
      val httpResponseMsg = HttpResponse(StatusCodes.OK, Nil, HttpEntity.Empty, HttpProtocols.`HTTP/1.1`)
      EventFilter.info(message = s"received HttpResponse OK(200): $httpResponseMsg", occurrences = 1) intercept {
        auctionClientActor ! httpResponseMsg
      }
    }
    "receive http response messages failures" in {
      val httpResponseMsg = HttpResponse(StatusCodes.BadRequest, Nil, HttpEntity.Empty, HttpProtocols.`HTTP/1.1`)
      EventFilter.info(message = s"Request failed, response code: ${httpResponseMsg.status}", occurrences = 1) intercept {
        auctionClientActor ! httpResponseMsg
      }
    }
  }
}
