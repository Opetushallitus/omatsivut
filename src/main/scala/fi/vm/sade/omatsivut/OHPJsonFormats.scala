package fi.vm.sade.omatsivut

import org.json4s.{DefaultFormats, Formats}

trait OHPJsonFormats {
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}
