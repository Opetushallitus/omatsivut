package fi.vm.sade.omatsivut

import java.text.SimpleDateFormat

import org.joda.time.DateTimeUtils

trait TimeWarp {
  def getMillisFromTime(dateTime: String) = {
    new SimpleDateFormat("d.M.yyyy HH:mm").parse(dateTime).getTime
  }

  def withFixedDateTime[T](dateTime: String)(f: => T):T = {
    withFixedDateTime(getMillisFromTime(dateTime))(f)
  }

  def withFixedDateTime[T](millis: Long)(f: => T) = {
    DateTimeUtils.setCurrentMillisFixed(millis)
    try {
      f
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem
    }
  }
}
