package fi.vm.sade.omatsivut.http

import fi.vm.sade.omatsivut.util.Logging

import scala.collection.immutable.HashMap
import scalaj.http.Http.Request
import scalaj.http.{Http, HttpException}

trait HttpRequest{
   def responseWithHeaders(): (Int, Map[String, List[String]], String)
   def response(): Option[String]
   def param(key: String, value: String): HttpRequest
}

class DefaultHttpRequest(private val request: Request) extends HttpRequest with Logging {

  def param(key: String, value: String) = {
    new DefaultHttpRequest(request.param(key, value))
  }

  def responseWithHeaders(): (Int, Map[String, List[String]], String) = {
    try {
      request.asHeadersAndParse(Http.readString)
    } catch {
      case e: HttpException => {
        if(e.code != 404) logUnexpectedError(e)
        (e.code, HashMap(), e.body)
      }
      case t: Throwable => {
        logUnexpectedError(t)
        (500, HashMap(), "")
      }
    }
  }

  def response(): Option[String] = {
    try {
      Some(request.asString)
    } catch {
      case e: HttpException => {
        if(e.code != 404) logUnexpectedError(e)
        None
      }
      case t: Throwable => {
        logUnexpectedError(t)
        None
      }
    }
  }

  private def logUnexpectedError(t: Throwable) {
    logger.error("Unexpected error from " + request.method + " to " + request.url + " : " + t, t)
  }
}