package org.github.felipegutierrez.biddingsystem.auction.util

import org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse
import spray.json.DefaultJsonProtocol.{jsonFormat3, _}
import spray.json._

/**
 * This is the [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse]] converter.
 */
object BidResponseConverter {
  implicit val BidOfferFormat = jsonFormat3(BidResponse)

  /**
   * The [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse]] is generated using the
   * [[https://doc.akka.io/japi/akka-http/current/akka/http/scaladsl/marshallers/sprayjson/SprayJsonSupport.html SprayJsonSupport]].
   *
   * @param json a JSON string in the format of the [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse]].
   * @return the [[org.github.felipegutierrez.biddingsystem.auction.message.BidProtocol.BidResponse]].
   */
  def getBidResponse(json: String): BidResponse = {
    val bidResponse = json.parseJson.convertTo[BidResponse]
    BidResponse(bidResponse.id, bidResponse.bid, bidResponse.content.replace("$price$", bidResponse.bid.toString))
  }
}
