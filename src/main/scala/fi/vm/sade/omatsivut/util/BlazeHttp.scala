package fi.vm.sade.omatsivut.util

import cats.effect.{IO, Resource}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import scala.concurrent.ExecutionContext.global

object BlazeHttpClient {

  /**
   * Creates a reusable Resource for an HTTP client.
   */
  def createHttpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global).resource

  /**
   * Runs an operation using a managed HTTP client.
   *
   * @param useClient A function that takes a `Client[IO]` and returns an `IO[A]`.
   * @tparam A The result type of the operation.
   */
  def withHttpClient[A](useClient: Client[IO] => IO[A]): IO[A] =
    createHttpClient.use(useClient)
}

