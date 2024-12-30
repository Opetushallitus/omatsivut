package fi.vm.sade.omatsivut.util

import cats.effect.{IO, Resource}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import scala.concurrent.ExecutionContext.global

object BlazeHttpClient {

  def createHttpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global).resource

}

