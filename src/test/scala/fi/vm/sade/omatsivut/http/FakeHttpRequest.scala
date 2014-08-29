package fi.vm.sade.omatsivut.http

class FakeHttpRequest extends HttpRequest {
  def responseWithHeaders(): (Int, Map[String, List[String]], String) = (200, Map[String, List[String]](), "")
  def response(): Option[String] = Some("")
  def param(key: String, value: String): HttpRequest = this
  def header(key: String, value: String): HttpRequest = this
}