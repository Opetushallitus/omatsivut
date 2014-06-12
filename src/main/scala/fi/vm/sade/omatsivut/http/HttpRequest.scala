package fi.vm.sade.omatsivut.http

import scalaj.http.Http
import scalaj.http.Http.Request
import java.io.FileNotFoundException
import scala.collection.immutable.HashMap
import scalaj.http.HttpException
import fi.vm.sade.omatsivut.Logging

class HttpRequest(private val request: Request) extends Logging {

  def param(key: String, value: String) = {
    new HttpRequest(request.param(key, value))
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