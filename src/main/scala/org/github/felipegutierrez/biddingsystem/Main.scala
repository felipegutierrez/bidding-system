package org.github.felipegutierrez.biddingsystem

import org.github.felipegutierrez.biddingsystem.auction.server.AuctionServer

object Main {

  def main(args: Array[String]): Unit = {
    println("\nThis is a Bidding system")
    if (args.isEmpty) {
      println("please pass the bidders as parameters when starting the Bidding system")
      println("example: sbt \"run http://localhost:8081 http://localhost:8082 http://localhost:8083\"\n")
    } else {
      println(s"The Auction system is starting with bidders: ${args.mkString(" ")}")
      AuctionServer.run()
    }
  }
}
