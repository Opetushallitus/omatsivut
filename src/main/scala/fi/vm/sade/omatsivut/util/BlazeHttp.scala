package fi.vm.sade.omatsivut.util

import cats.effect.{IO, Resource}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import scala.concurrent.duration._

object BlazeHttpClient {

  lazy val httpClientResource: Resource[IO, Client[IO]] = BlazeHttpClient.createHttpClient

  // Tätä käytetään vain testien fakeclientissa
  def getClient: Client[IO] = httpClientResource.use(IO.pure).unsafeRunSync()(cats.effect.unsafe.implicits.global)

  /**
   * Creates a reusable Resource for an HTTP client.
   */
  def createHttpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ThreadPools.httpExecutionContext)
      .withMaxTotalConnections(50)
      .withMaxWaitQueueLimit(1024)
      .withConnectTimeout(15.seconds)
      .withResponseHeaderTimeout(30.seconds)
      .withRequestTimeout(1.minute)
      .withIdleTimeout(2.minutes)
      .resource

}

