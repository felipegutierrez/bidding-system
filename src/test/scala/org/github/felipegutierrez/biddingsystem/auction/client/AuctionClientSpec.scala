package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.github.felipegutierrez.biddingsystem.auction.message.Bid
import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class AuctionClientSpec extends TestKit(ActorSystem("AuctionClientSpec"))
  with Matchers
  with ImplicitSender
  with AnyWordSpecLike
  with MockFactory
  with ScalaFutures
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "the auction client actor" should {
    val bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
    val auctionClientActor = system.actorOf(AuctionClientActor.props(bidders), "auctionClientActorSpec")
    "receive bid requests" in {
      val bidRequestMsg = BidRequest(UUID.randomUUID().toString, Bid(1, Map("b" -> "5", "c" -> "10")))
      EventFilter.info(message = s"received bid request: $bidRequestMsg", occurrences = 1) intercept {
        auctionClientActor ! bidRequestMsg
      }
    }
    "log unknown messages" in {
      val message = "this case is not supported"
      EventFilter.warning(message = s"unknown message: $message", occurrences = 1) intercept {
        auctionClientActor ! message
      }
    }
  }
}
