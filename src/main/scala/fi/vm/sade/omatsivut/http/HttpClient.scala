package fi.vm.sade.omatsivut.http

import scalaj.http.Http
import scalaj.http.HttpOptions


trait HttpClient {

  def httpGet(url: String) : HttpRequest = {
    new HttpRequest(Http.get(url)
  		.options(HttpOptions.connTimeout(5000))
  		.option(HttpOptions.readTimeout(10000))
    )
  }

  def httpPost(url: String) : HttpRequest = {
    new HttpRequest(Http.post(url)
  		.options(HttpOptions.connTimeout(5000))
  		.option(HttpOptions.readTimeout(10000))
    )
  }
  
}