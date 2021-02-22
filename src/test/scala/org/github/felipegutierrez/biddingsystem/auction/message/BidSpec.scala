package org.github.felipegutierrez.biddingsystem.auction.message

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.scalatest.flatspec.AnyFlatSpec
import spray.json._

class BidSpec extends AnyFlatSpec
  with BidJsonProtocol with SprayJsonSupport {

  "the Bid Request JSON converter using Spray JSON" should
    "convert correct json files" in {

    val attributes: Map[String, String] = Map("a" -> "1", "c" -> "2")
    val bid = Bid(1, attributes)

    val jsonExpected = "{\"attributes\":{\"a\":\"1\",\"c\":\"2\"},\"id\":1}"
    println(bid.toJson.toString)
    assertResult(jsonExpected)(bid.toJson.toString)
  }
}
