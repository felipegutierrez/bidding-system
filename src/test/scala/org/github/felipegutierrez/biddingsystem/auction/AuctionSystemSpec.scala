package org.github.felipegutierrez.biddingsystem.auction

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class AuctionSystemSpec
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest {

  import AuctionSystem._

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(3 seconds)

  "The Auction system" should {
    "allow routes that contains only the ad ID as a number on the path" in {
      // send an HTTP request through an endpoint that you want to test
      Get("/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/123") ~> routes ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/") ~> routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Get("/string") ~> routes ~> check {
        status shouldBe StatusCodes.Forbidden
      }
      Post("/123") ~> routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
      Put("/123") ~> routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
      Delete("/123") ~> routes ~> check {
        // status shouldBe StatusCodes.BadRequest
        rejections should not be empty // "natural language" style
        rejections.should(not).be(empty) // same

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }
        methodRejections.length shouldBe 1
      }
    }
  }
}
