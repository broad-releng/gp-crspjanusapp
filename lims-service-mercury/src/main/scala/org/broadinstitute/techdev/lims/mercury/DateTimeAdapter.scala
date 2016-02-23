package org.broadinstitute.techdev.lims.mercury

import java.util.{GregorianCalendar, Date}
import javax.xml.bind.DatatypeConverter

/**
 * User: John Walsh
 * Date: 8/15/12
 */

object DateTimeAdapter {
  def parseDate(s : String) : Date = {
    if ( s == null )
      null
    else
      DatatypeConverter.parseDateTime(s).getTime
  }

  def printDate(dt : Date) : String = {
    if(dt == null)
      return ""

    val cal = new GregorianCalendar()
    cal.setTime(dt)
    DatatypeConverter.printDateTime(cal)
  }
}