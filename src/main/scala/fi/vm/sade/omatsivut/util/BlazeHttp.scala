package fi.vm.sade.omatsivut.util

import cats.effect.{IO, Resource}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import scala.concurrent.ExecutionContext.global

object BlazeHttpClient {

  lazy val httpClientResource: Resource[IO, Client[IO]] = BlazeHttpClient.createHttpClient

  def getClient: Client[IO] = httpClientResource.use(IO.pure).unsafeRunSync()(cats.effect.unsafe.implicits.global)

  /**
   * Creates a reusable Resource for an HTTP client.
   */
  def createHttpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global).resource

}

