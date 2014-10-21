package fi.vm.sade.omatsivut

import java.text.SimpleDateFormat

import org.joda.time.DateTimeUtils

trait TimeWarp {
  def getMillisFromTime(dateTime: String) = {
    new SimpleDateFormat("d.M.yyyy HH:mm").parse(dateTime).getTime
  }

  def withFixedDateTime[T](dateTime: String)(f: => T) = {
    DateTimeUtils.setCurrentMillisFixed(getMillisFromTime(dateTime))
    try {
      f
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem
    }
  }
}
