package org.github.felipegutierrez.biddingsystem.auction.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class AuctionServerSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest {
  val auctionServer = new AuctionServer()

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(3 seconds)

  "The Auction system" should {
    "allow routes that contains only the ad ID as a number on the path" in {
      // send an HTTP request through an endpoint that you want to test
      Get("/1") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/123") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Get("/string") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.Forbidden
      }
      Get("/2?c=5&b=2") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
        // the bidders are not up so there will be no winner
        responseAs[Option[String]] shouldBe Some("")
      }
      Post("/123") ~> auctionServer.routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
      Put("/123") ~> auctionServer.routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
      Delete("/123") ~> auctionServer.routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
    }
    "allow parameters as a list of key=value pairs" in {
      Get("/1?a=5") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/1?a=5&b=10") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/1?a=5&a=5&b=10") ~> auctionServer.routes ~> check {
        status shouldBe StatusCodes.OK
      }
    }
  }

  "a Auction Server singleton object" should {
    "have the same list of bidders of an instance Auction Server" in {
      val auctionServerSingleton = AuctionServer.apply()
      assertResult(auctionServerSingleton.getBidders())(auctionServer.getBidders())
    }
  }
}
