package org.github.felipegutierrez.biddingsystem.auction.protocol

import org.github.felipegutierrez.biddingsystem.auction.Bid

object BidProtocol {

  case class BidRequest(requestId: String, bid: Bid)

}
