package org.github.felipegutierrez.biddingsystem.auction.message

import spray.json._

case class Bid(id: Int, attributes: Map[String, String])

trait BidJsonProtocol extends DefaultJsonProtocol {
  implicit val bidFormat = jsonFormat2(Bid)
}
