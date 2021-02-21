package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.ActorSystem
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
  }
}
