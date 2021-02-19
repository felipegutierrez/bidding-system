package org.github.felipegutierrez.biddingsystem.auction

import spray.json._

case class Bid(adId: Int, params: Seq[(String, String)])

trait BidJsonProtocol extends DefaultJsonProtocol {
  implicit val bidFormat = jsonFormat2(Bid)
}
