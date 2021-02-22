package org.github.felipegutierrez.biddingsystem

import org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer

import scala.sys.exit

object Main {

  def main(args: Array[String]): Unit = {
    println("\nThis is a Bidding system")

    if (args.length == 0) {
      println("please pass the bidders as argument when starting the Bidding system")
      println("example: sbt \"run --bidders http://localhost:8081,http://localhost:8082,http://localhost:8083\"\n")
    } else {
      val arglist = args.toList
      type OptionMap = Map[Symbol, Any]

      def nextOption(map: OptionMap, list: List[String]): OptionMap = {
        list match {
          case Nil => map
          case "--bidders" :: value :: tail =>
            nextOption(map ++ Map('bidders -> value.toString), tail)
          case option :: tail =>
            println("Unknown option " + option)
            exit(1)
        }
      }

      val options = nextOption(Map(), arglist)
      val bidders = options.get('bidders).getOrElse("").toString.split(",").toList
      println(s"The Auction system is starting with bidders: ${bidders.mkString(", ")}")
      new AuctionServer(bidders).run()
    }
  }
}
