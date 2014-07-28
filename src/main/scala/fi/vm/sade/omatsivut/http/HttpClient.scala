package fi.vm.sade.omatsivut.http

import scalaj.http.{Http, HttpOptions}

trait HttpClient {
  def httpGet(url: String) : HttpRequest
  def httpPost(url: String) : HttpRequest
}

object DefaultHttpClient extends HttpClient {
  def httpGet(url: String) : HttpRequest = {
    new DefaultHttpRequest(Http.get(url)
  		.options(HttpOptions.connTimeout(5000))
  		.option(HttpOptions.readTimeout(10000))
    )
  }

  def httpPost(url: String) : HttpRequest = {
    new DefaultHttpRequest(Http.post(url)
  		.options(HttpOptions.connTimeout(5000))
  		.option(HttpOptions.readTimeout(10000))
    )
  }
}