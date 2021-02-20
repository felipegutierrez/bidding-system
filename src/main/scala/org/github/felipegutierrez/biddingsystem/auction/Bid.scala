package org.github.felipegutierrez.biddingsystem.auction

import spray.json._

case class Bid(id: Int, attributes: List[(String, String)])

trait BidJsonProtocol extends DefaultJsonProtocol {
  implicit val bidFormat = jsonFormat2(Bid)
}
