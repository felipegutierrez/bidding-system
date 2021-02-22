package org.github.felipegutierrez.biddingsystem.auction.util

import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse
import org.scalatest.flatspec.AnyFlatSpec

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
}
