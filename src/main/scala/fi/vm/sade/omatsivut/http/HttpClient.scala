package fi.vm.sade.omatsivut.http

import scalaj.http.{Http, HttpOptions}

trait HttpClient {
  def httpGet(url: String) : HttpRequest
  def httpPost(url: String, data: Option[String]) : HttpRequest
}

object DefaultHttpClient extends HttpClient {
  def httpGet(url: String) : HttpRequest = {
    new DefaultHttpRequest(changeOptions(Http.get(url)))
  }

  def httpPost(url: String, data: Option[String]) : HttpRequest = {
    new DefaultHttpRequest(changeOptions(data match {
      case None => Http.post(url)
      case Some(data) => Http.postData(url, data)
    }))
  }

  def httpPut(url: String) : HttpRequest = {
    new DefaultHttpRequest(Http(url).method("put"))
  }

  private def changeOptions(request: Http.Request): Http.Request = {
    request
      .options(HttpOptions.connTimeout(10000))
      .option(HttpOptions.readTimeout(60000))
  }
}