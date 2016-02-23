package org.broadinstitute.techdev.lims.mercury

/**
 * User: John Walsh
 * Date: 8/15/12
 */
import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

class DateAdapater  extends XmlAdapter[String, Date]
{
  def  unmarshal(value : String) : Date = {
    (DateTimeAdapter.parseDate(value))
  }

  def marshal(value:Date) : String = {
    (DateTimeAdapter.printDate(value))
  }
}
