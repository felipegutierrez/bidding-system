package org.github.felipegutierrez.biddingsystem.auction.message

object BidProtocol {

  case class BidRequest(requestId: String, bid: Bid)

  case class BidResponse(id: String, bid: Int, content: String)

}
