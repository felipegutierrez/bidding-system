package org.github.felipegutierrez.biddingsystem

import com.typesafe.scalalogging.Logger
import org.github.felipegutierrez.biddingsystem.BiddingSystem.BiddingSystem
import org.junit.runner.RunWith
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.slf4j.{Logger => Underlying}

@RunWith(classOf[JUnitRunner])
class BiddingSystemSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  def initTestableBiddingSystem(mocked: Underlying): BiddingSystem = {
    new BiddingSystem() {
      override lazy val logger = Logger(mocked)
    }
  }

  "the bidding system main class" should
    "not start with no arguments" in {
    val mocked = Mockito.mock(classOf[Underlying])

    when(mocked.isInfoEnabled()).thenReturn(true)

    val option = Array[String]()
    initTestableBiddingSystem(mocked).run(option)

    verify(mocked).info("please pass the bidders as argument when starting the Bidding system")
  }

  "the bidding system main class" should
    "not start with empty argument list" in {
    val mocked = Mockito.mock(classOf[Underlying])

    when(mocked.isInfoEnabled()).thenReturn(true)

    val option = Array[String] { "" }
    initTestableBiddingSystem(mocked).run(option)

    verify(mocked).info("Unknown option: " + option(0))
  }
}
