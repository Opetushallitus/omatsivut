package fi.vm.sade.omatsivut.http

import fi.vm.sade.hakemuseditori.http.UrlValueCompressor
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UrlValueCompressorSpec extends Specification {

  "Compressing" should {

    "compress and decompress test json" in {
      val testString = """["1.2.246.562.14.2014032511552174082953","1.2.246.562.20.40315749842","1.2.246.562.20.52933873672"]"""
      val compressed = UrlValueCompressor.compress(testString)
      compressed.length must be_<(testString.length)
      UrlValueCompressor.decompress(compressed) must_== testString
    }

    "compress and decompress test json with line breaks" in {
      val testString = "[\"1.2.246.562.14.2014032511552174082953\",\n\"1.2.246.562.20.40315749842\",\r\"1.2.246.562.20.52933873672\"]"
      val compressed = UrlValueCompressor.compress(testString)
      compressed.length must be_<(testString.length)
      UrlValueCompressor.decompress(compressed) must_== testString.replace('\r','\n')
    }
  }
}
