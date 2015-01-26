import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import scalaj.http.{HttpOptions, Http}

trait TestHelpers {
  implicit val formats = DefaultFormats

  def retry[T](n: Int = 10, sleepMs: Int = 200)(fn: => T): T = {
    try {
      fn
    } catch {
      case e: Throwable =>
        if (n > 1) {
          Thread.sleep(sleepMs)
          retry(n - 1)(fn)
        }
        else throw e
    }
  }

  def get(url: String) = {
    val response = Http(url).asString
    if (response.code != 200) {
      throw new RuntimeException(s"Get $url status code was $response.code")
    }
    response
  }

  def postJson[A <: AnyRef](url: String, data: A) {
    val response = Http(url).postData(Serialization.write(data))
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))
      .option(HttpOptions.connTimeout(10000))
      .asString
    if (response.code != 200) {
      throw new RuntimeException(s"Get $url status code was $response.code")
    }
  }

  def assertContainsAll(s: String, args: String*) = {
    val notFound = args.filter((arg) => {
      !s.contains(arg)
    })
    if(notFound.nonEmpty) {
      throw new RuntimeException(s"Could not find '${notFound.mkString("', '")}' from $s")
    }
  }
}
