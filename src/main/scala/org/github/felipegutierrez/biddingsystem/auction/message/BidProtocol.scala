package org.github.felipegutierrez.biddingsystem.auction.message

/**
 * This is the bid protocol which contains the messages used between the [[org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer]]
 * and [[org.github.felipegutierrez.biddingsystem.auction.client.AuctionClientActor]].
 */
object BidProtocol {

  /**
   * The bid request generated on the [[org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer]]
   *
   * @param requestId a random string
   * @param bid       the [[org.github.felipegutierrez.biddingsystem.auction.message.Bid]] object.
   */
  case class BidRequest(requestId: String, bid: Bid)

  /**
   * The bid response received from the bidders and that can be converted to the JSON format:
   * {{{
   *   {
   *   	"id" : "id",
   *   	"bid": bid,
   *   	"content": "the string to deliver as a response"
   *   }
   * }}}
   *
   * @param id      the bid id
   * @param bid     the bid, how much the bidders is offering for a request
   * @param content the content is the [[bid]] converted to a string done at [[org.github.felipegutierrez.biddingsystem.auction.util.BidResponseConverter]]
   */
  case class BidResponse(id: String, bid: Int, content: String)

}
