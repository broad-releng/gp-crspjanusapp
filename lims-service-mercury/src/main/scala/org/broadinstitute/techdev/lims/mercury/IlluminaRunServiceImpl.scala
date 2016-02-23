package org.broadinstitute.techdev.lims.mercury

import com.sun.jersey.api.client.{Client, ClientResponse}
import java.net.HttpURLConnection
import com.sun.jersey.core.util.MultivaluedMapImpl
import com.google.gson._
import java.util.Date
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}
import javax.xml.bind.DatatypeConverter
import com.google.gson.reflect.TypeToken
import javax.ws.rs.core.MediaType

import org.broadinstitute.techdev.lims.mercury.{SolexaRunBean, ZimsIlluminaRunType}

/**
  * Client Resource for IlluminaRunService
  */
class IlluminaRunServiceImpl(hostname: String) extends IlluminaRunService {
  private val SOLEXA_RUN_BASE_URL = s"https://$hostname/Mercury/rest/solexarun/"

  private var mClient = Client.create()

  private val mDateDeser = new JsonDeserializer[Date]() {
    def deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, context: JsonDeserializationContext) = {
      if(json == null)
        null
      else
        DatatypeConverter.parseDateTime(json.getAsString).getTime
    }
  }

  private val mXmlGregorianCalendarDeser = new JsonDeserializer[XMLGregorianCalendar] {
    def deserialize(jsonElement: JsonElement, typeOfT: java.lang.reflect.Type, context: JsonDeserializationContext) = {
      try{
        DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonElement.getAsString)
      }catch {
        case e:Exception => null
      }
    }
  }

  private val mXmlGregorianCalendarSer = new JsonSerializer[XMLGregorianCalendar] {
    override def serialize(id: XMLGregorianCalendar, typeOfId: java.lang.reflect.Type, context: JsonSerializationContext): JsonElement = {
      new JsonPrimitive(id.toXMLFormat)
    }
  }

  private val mGson =
    new GsonBuilder().
      registerTypeAdapter(classOf[Date], mDateDeser).
      registerTypeAdapter(classOf[XMLGregorianCalendar], mXmlGregorianCalendarDeser).
      registerTypeAdapter(classOf[XMLGregorianCalendar], mXmlGregorianCalendarSer).
      create()

  private val mZimsIlluminaRun = new TypeToken[ZimsIlluminaRunType]() {}.getType

  def createRun(solexaRunBean : SolexaRunBean) : Boolean = {
    val url = SOLEXA_RUN_BASE_URL
    val service = mClient.resource(url)
    val response = service.`type`(MediaType.APPLICATION_XML_TYPE).
      accept(MediaType.APPLICATION_XML_TYPE).
      entity(solexaRunBean).
      post(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_CREATED =>
        true
      case _ =>
        false
    }
  }

  override def queryRun(runName: String): ZimsIlluminaRunType = {
    val url = s"https://$hostname/Mercury/rest/IlluminaRun/query"
    val params = new MultivaluedMapImpl()
    params.add("runName", runName)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mZimsIlluminaRun)
      case _ =>
        null
    }
  }
}
