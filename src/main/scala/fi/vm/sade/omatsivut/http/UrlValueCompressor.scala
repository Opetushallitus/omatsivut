package fi.vm.sade.omatsivut.http

import java.io._

import org.apache.commons.codec.binary.Base64
import org.apache.commons.compress.compressors.deflate.{DeflateCompressorInputStream, DeflateCompressorOutputStream}

object UrlValueCompressor {

  def compress(value: String) = {
    val out = new ByteArrayOutputStream()
    val compressor = new DeflateCompressorOutputStream(out)
    compressor.write(value.getBytes("UTF-8"))
    compressor.close()
    Base64.encodeBase64URLSafeString(out.toByteArray)
  }

  def decompress(compressed: String) = {
    val reader = new BufferedReader(new InputStreamReader(new DeflateCompressorInputStream(new ByteArrayInputStream(Base64.decodeBase64(compressed))), "UTF-8"))
    val value = new StringBuffer()
    var newLine = reader.readLine()
    while(newLine != null) {
      if(value.length() > 0) {
        value.append('\n')
      }
      value.append(newLine)
      newLine = reader.readLine()
    }
    value.toString
  }
}
