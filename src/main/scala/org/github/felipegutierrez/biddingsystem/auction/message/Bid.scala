package org.github.felipegutierrez.biddingsystem.auction.message

import spray.json._

/**
 * The bid that is defined by the JSON format:
 * {{{
 *   {
 *   	"id": id,
 *   	"attributes" : { "key": "value", ... }
 *   }
 * }}}
 *
 * @param id         the bid ID
 * @param attributes the bid attributes as a Map of [String, String]
 */
case class Bid(id: Int, attributes: Map[String, String])

trait BidJsonProtocol extends DefaultJsonProtocol {
  implicit val bidFormat = jsonFormat2(Bid)
}
