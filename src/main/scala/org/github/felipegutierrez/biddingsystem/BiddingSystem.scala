package org.github.felipegutierrez.biddingsystem

import com.typesafe.scalalogging.LazyLogging
import org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer

/**
 * This is the entry point of the Bidding System.
 */
object BiddingSystem {

  def main(args: Array[String]): Unit = {
    val v = new BiddingSystem()
    v.run(args)
  }

  /**
   * The Bidding System main class with logging
   */
  class BiddingSystem extends LazyLogging {
    /**
     * The method that receives arguments to start the Bidding System with a given number of bidders.
     * The bidders must be passed as argument following the pattern "--bidders [host_of_the_bidder_split_by_comma_without_space]"
     * Example: "--bidders http://localhost:8081,http://localhost:8082,http://localhost:8083"
     *
     * @param args the bidders split by comma and without space
     */
    def run(args: Array[String]): Unit = {
      println("\nThis is a Bidding system")

      if (args.length == 0) {
        logger.info("please pass the bidders as argument when starting the Bidding system")
        logger.info("example: sbt \"run --bidders http://localhost:8081,http://localhost:8082,http://localhost:8083\"\n")
      } else {
        val arglist = args.toList
        type OptionMap = Map[Symbol, Any]

        def nextOption(map: OptionMap, list: List[String]): OptionMap = {
          list match {
            case Nil => map
            case "--bidders" :: value :: tail =>
              nextOption(map ++ Map('bidders -> value.toString), tail)
            case option :: tail =>
              logger.info("Unknown option: " + option)
              map
          }
        }

        val options = nextOption(Map(), arglist)
        val bidders = options.get('bidders).getOrElse("").toString.split(",").toList
        logger.info("The Auction system is starting with bidders: " + bidders.mkString(", "))
        new AuctionServer(bidders).run()
      }
    }
  }

}
