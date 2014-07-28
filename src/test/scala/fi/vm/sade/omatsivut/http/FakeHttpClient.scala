package fi.vm.sade.omatsivut.http

class FakeHttpClient(fakeRequest: HttpRequest) extends HttpClient {
  def httpGet(url: String) : HttpRequest = fakeRequest
  def httpPost(url: String) : HttpRequest = fakeRequest
}