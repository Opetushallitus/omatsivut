package fi.vm.sade.omatsivut.json

import org.json4s.{DefaultFormats, Formats}

trait JsonFormats {
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}
