package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import org.github.felipegutierrez.biddingsystem.auction.message.Bid
import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID
import scala.concurrent.Future

class HttpAuctionRestClientSpec extends TestKit(ActorSystem("AuctionClientSpec"))
  with Matchers
  with ImplicitSender
  with AnyWordSpecLike
  with MockFactory
  with ScalaFutures
  with BeforeAndAfterAll {

  class MockHttpAuctionClient extends HttpAuctionClient {
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
      mock(httpRequest)
    }
  }

  "Auction Rest Client" should {
    "work on HttpClient" in {
      // mock Http
      val bidders: List[String] = List[String]("http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
      val mockHttpAuctionClient: MockHttpAuctionClient = new MockHttpAuctionClient()

      val content =
        """
          |{
          |	"id": 1,
          |	"attributes" : { "a": "5" }
          |}
          |""".stripMargin
      val expectedContent: String =
        """
          |[{
          |	"id" : "1",
          |	"bid": 750,
          |	"content": "a:$price$"
          |}]""".stripMargin

      mockHttpAuctionClient.mock
        .expects(HttpRequest( // create the request
          HttpMethods.POST,
          uri = Uri(bidders(0)),
          entity = HttpEntity(ContentTypes.`application/json`, content)
        ))
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(expectedContent)))))

      val httpRequest: HttpRequest = HttpRequest( // create the request
        HttpMethods.POST,
        uri = Uri(bidders(0)),
        entity = HttpEntity(ContentTypes.`application/json`, content)
      )
      whenReady(mockHttpAuctionClient.sendRequest(httpRequest)) { res =>
        println(res)
        inAnyOrder {
          res must equal(HttpResponse(entity = HttpEntity(ByteString(expectedContent))))
        }
      }

      val mockAuctionClientActor = system.actorOf(AuctionClientActor.props(bidders, mockHttpAuctionClient), "mockAuctionClientActorSpec")
      val bidRequestMsg = BidRequest(UUID.randomUUID().toString, Bid(1, Map("a" -> "5")))
      mockAuctionClientActor ! bidRequestMsg
    }
  }
}
