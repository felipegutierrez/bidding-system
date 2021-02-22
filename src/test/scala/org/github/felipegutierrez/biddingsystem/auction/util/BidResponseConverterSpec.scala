package org.github.felipegutierrez.biddingsystem.auction.util

import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse
import org.scalatest.flatspec.AnyFlatSpec
import spray.json.DeserializationException

class BidResponseConverterSpec extends AnyFlatSpec {

  "the Bid Response JSON converter" should
    "convert a JSON string to a Bid Response with the bid value in the content field" in {
    val json: String =
      """
        |{
        |	"id" : "123",
        |	"bid": 750,
        |	"content": "a:$price$"
        |}""".stripMargin
    val bidResponseExpected: BidResponse = BidResponse("123", 750, "a:750")
    val bidResponse: BidResponse = BidResponseConverter.getBidResponse(json)
    assertResult(bidResponseExpected)(bidResponse)
  }

  "the Bid Response JSON converter with wrong json file" should
    "throws a DeserializationException" in {
    assertThrows[DeserializationException] {
      val json: String =
        """
          |{
          |	"id" : "123",
          |	"bid": 750
          |}""".stripMargin
      BidResponseConverter.getBidResponse(json)
    }
  }

  "the Bid Response JSON converter with wrong json attribute name" should
    "throws a DeserializationException" in {
    assertThrows[DeserializationException] {
      val json: String =
        """
          |{
          |	"id" : "123",
          |	"bid": 750,
          |	"contentttttt": "a:$price$"
          |}""".stripMargin
      BidResponseConverter.getBidResponse(json)
    }
  }
}
