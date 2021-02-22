package org.github.felipegutierrez.biddingsystem.auction.util

import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse
import spray.json.DefaultJsonProtocol.{jsonFormat3, _}
import spray.json._

object BidResponseConverter {
  implicit val BidOfferFormat = jsonFormat3(BidResponse)

  def getBidResponse(json: String): BidResponse = {
    val bidResponse = json.parseJson.convertTo[BidResponse]
    BidResponse(bidResponse.id, bidResponse.bid, bidResponse.content.replace("$price$", bidResponse.bid.toString))
  }
}
