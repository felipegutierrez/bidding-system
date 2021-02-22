package org.github.felipegutierrez.biddingsystem.auction.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

import scala.concurrent.duration._

class AuctionServerSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest {

  import AuctionServer._
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

  "The auction that converts a bid to json string" should {
    "return the correct json format" in {
      val bid = auctionServer.getBid(2, Seq(("c", "5"), ("b", "2")))
      // println(s"bid request: ${bid.toJson.prettyPrint}")
    }
  }
}
