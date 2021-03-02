package org.github.felipegutierrez.biddingsystem.auction.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future

/**
 * The trait HttpAuctionClient is used to facilitate the mocking tests when trying to send http requests to a third part server (the bidders).
 * So, we simulate the bidders response
 */
trait HttpAuctionClient {
  /**
   * The [[AuctionClientActor]] will send requests using this method.
   *
   * @param httpRequest the http request to the bidder
   * @param actorSystem the default actor system of the [[AuctionClientActor]]
   * @return
   */
  def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse]
}

/**
 * The [[HttpAuctionRestClient]] class extends the trait [[HttpAuctionClient]] to ensure an implementation of the
 * "sendRequest(httpRequest: HttpRequest)" method.
 */
class HttpAuctionRestClient extends HttpAuctionClient {
  /**
   * This is the default implementation of the http requests to a bidder.
   * If the mocked test want to mock the behavior of a bidder it must implement this method and extend the trait [[HttpAuctionClient]].
   *
   * @param httpRequest the http request to the bidder
   * @param actorSystem the default actor system of the [[AuctionClientActor]]
   * @return
   */
  override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    Http(actorSystem).singleRequest(httpRequest)
  }
}
