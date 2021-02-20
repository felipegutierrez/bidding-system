package org.github.felipegutierrez.biddingsystem.auction

import spray.json.DefaultJsonProtocol.{jsonFormat3, _}
import spray.json._

object BidOfferConverter {
  implicit val BidOfferFormat = jsonFormat3(BidOffer)

  def getBidOffer(json: String): BidOffer = {
    json.parseJson.convertTo[BidOffer]
  }
}

case class BidOffer(id: String, bid: Int, content: String)
