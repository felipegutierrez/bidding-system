package org.github.felipegutierrez.biddingsystem.auction

import spray.json.DefaultJsonProtocol.{jsonFormat3, _}
import spray.json._

object BidOfferConverter {
  implicit val BidOfferFormat = jsonFormat3(BidOffer)

  def getBidOffer(json: String): BidOffer = {
    val bidOffer = json.parseJson.convertTo[BidOffer]
    BidOffer(bidOffer.id, bidOffer.bid, bidOffer.content.replace("$price$", bidOffer.bid.toString))
  }
}

case class BidOffer(id: String, bid: Int, content: String)
