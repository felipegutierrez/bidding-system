package org.github.felipegutierrez.biddingsystem

import org.github.felipegutierrez.biddingsystem.auction.AuctionSystem

object Main {

  def main(args: Array[String]): Unit = {
    println("\nThis is a bidding system")
    println("The Auction system is starting ... ")
    AuctionSystem.run()
  }
}
