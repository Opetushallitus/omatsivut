package fi.vm.sade.omatsivut.http

import scalaj.http.Http
import scalaj.http.Http.Request

class HttpRequest(private val request: Request) {
	
  def param(key: String, value: String) = {
    new HttpRequest(request.param(key, value))
  }
  
  def responseWithHeaders(): (Int, Map[String,List[String]], String) = {
    request.asHeadersAndParse(Http.readString)
  }
  
  def response(): String = {
    request.asString
  }
}