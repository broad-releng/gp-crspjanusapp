package org.broadinstitute.techdev.lims.mercury

import java.net.HttpURLConnection
import com.sun.jersey.api.client.{ClientResponse, Client}
import com.google.gson._
import com.google.gson.reflect.TypeToken
import scala.collection.JavaConversions._
import com.sun.jersey.core.util.MultivaluedMapImpl
import java.util.Date
import javax.xml.bind.DatatypeConverter
import java.lang.reflect
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}
import java.util
import javax.net.ssl._
import com.sun.jersey.api.client.config.{DefaultClientConfig, ClientConfig}
import com.sun.jersey.client.urlconnection.HTTPSProperties
import scala.Some
import java.lang.reflect.Type

/**
  * LimsQueries client
  */
class LimsServiceMercuryImpl(hostname : String = "mercury.broadinstitute.org:8443",  protocol : String = "https") extends LimsService {
  def this(hostname : String) = this(hostname, "https")
  def this() = this("mercury.broadinstitute.org:8443")

  private var BASE_URL = s"$protocol://$hostname/Mercury/rest/limsQuery/"

  private val mHostnameVerifier = new HostnameVerifier {
    override def verify(p1: String, p2: SSLSession): Boolean = {
      true
    }
  }

  val config = acceptAllServerCertificates()
  private var mClient = Client.create(config)

  private val TRUSEQ_SS_FAST_TRACK_PRODUCT_NAME = "Fast Track Strand Specific RNA Sequencing - High Coverage (50M pairs)"

  protected def acceptAllServerCertificates() : ClientConfig = {
    try {
      val config: ClientConfig = new DefaultClientConfig
      val sc: SSLContext = SSLContext.getInstance("TLS")
      sc.init(null, null, null)
      config.getProperties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(mHostnameVerifier, sc))
      config
    }
    catch {
      case e: Exception =>
        throw new RuntimeException(e)
    }
  }

  def sslInit(){}

  private val mDateDeser = new JsonDeserializer[Date]() {
    def deserialize(json: JsonElement, typeOfT: reflect.Type, context: JsonDeserializationContext) = {
      if(json == null) null else DatatypeConverter.parseDateTime(json.getAsString).getTime
    }
  }

  private val mXmlGregorianCalendarDeser = new JsonDeserializer[XMLGregorianCalendar] {
    def deserialize(jsonElement: JsonElement, typeOfT: reflect.Type, context: JsonDeserializationContext) = {
      try{
        DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonElement.getAsString)
      }catch {
        case e:Exception => null
      }
    }
  }

  private val mXmlGregorianCalendarSer = new JsonSerializer[XMLGregorianCalendar] {
    override def serialize(id: XMLGregorianCalendar, typeOfId: Type, context: JsonSerializationContext): JsonElement = {
      new JsonPrimitive(id.toXMLFormat)
    }
  }

  private val mGson =
    new GsonBuilder().
      registerTypeAdapter(classOf[Date], mDateDeser).
      registerTypeAdapter(classOf[XMLGregorianCalendar], mXmlGregorianCalendarDeser).
      registerTypeAdapter(classOf[XMLGregorianCalendar], mXmlGregorianCalendarSer).
      create()

  private val mStringCollectionType = new TypeToken[java.util.List[String]]() {}.getType

  private val mLibraryCollectionType = new TypeToken[java.util.List[LibraryDataType]]() {}.getType

  private val mConcAndVolCollectionType = new TypeToken[java.util.Map[String, ConcentrationAndVolumeAndWeightType]]() {}.getType

  private val mMapCollectionType = new TypeToken[java.util.Map[String, Boolean]]() {}.getType

  private val mFlowcellDesignationType = new TypeToken[FlowcellDesignationType]() {}.getType

  private val mPlateTransfersType = new TypeToken[java.util.List[PlateTransferType]]() {}.getType

  private val mWellAndSourceTubeType = new TypeToken[java.util.List[WellAndSourceTubeType]]() {}.getType

  private val mSeqTemplate = new TypeToken[SequencingTemplateType]() {}.getType

  def setBaseUrl(url : String){BASE_URL = url}

  def fetchLibraryDetailsByTubeBarcode(tubeBarcodes: java.util.List[String], includeWorkRequestDetails: Boolean) = {
    val params = new MultivaluedMapImpl()
    tubeBarcodes.foreach(params.add("q", _))
    params.add("includeWorkRequestDetails", includeWorkRequestDetails)
    val url = BASE_URL + "fetchLibraryDetailsByTubeBarcode"
    println(url)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mLibraryCollectionType)
      case _ =>
        List()
    }
  }


  override def fetchConcentrationAndVolumeAndWeightForTubeBarcodes(tubeBarcodes: util.List[String]): util.Map[String, ConcentrationAndVolumeAndWeightType] = {
    val params = new MultivaluedMapImpl()
    tubeBarcodes.foreach(params.add("q", _))
    val timestamp = new Date().getTime
    params.add("cachebuster", timestamp)
    val url = BASE_URL + "fetchConcentrationAndVolumeAndWeightForTubeBarcodes"
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mConcAndVolCollectionType)
      case _ =>
        Map[String, ConcentrationAndVolumeAndWeightType]()
    }
  }

  def doesLimsRecognizeAllTubes(tubeBarcodes: Iterable[String]) = {
    val params = new MultivaluedMapImpl()
    tubeBarcodes.foreach(params.add("q", _))
    val url = BASE_URL + "doesLimsRecognizeAllTubes"
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        responseJson equals "true"
      case _ =>
        false
    }
  }

  def findFlowcellDesignationByTaskName(taskName: String) = {
    findFlowcellDesignation(BASE_URL + "findFlowcellDesignationByTaskName?taskName=" + taskName)
  }

  def fetchUnfulfilledDesignations = null //TODO Not implemented by breilly yet

  def findRelatedDesignationsForAnyTube(tubeBarcodes: List[String]) = { //TODO Still returns 204
  val params = new MultivaluedMapImpl()
    tubeBarcodes.foreach(params.add("q", _))
    val url = BASE_URL + "findRelatedDesignationsForAnyTube"
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val fromJson = response.getEntity(classOf[String])
        mGson.fromJson(fromJson, classOf[List[String]])
      case _ =>
        List()
    }
  }

  def findFlowcellDesignationByFlowcellBarcode(flowcellBarcode: String) = {
    findFlowcellDesignation(BASE_URL + "findFlowcellDesignationByFlowcellBarcode?flowcellBarcode=" + flowcellBarcode)
  }

  def findFlowcellDesignationByReagentBlockBarcode(reagentKitBarcode: String) = {
    findFlowcellDesignation(BASE_URL + "findFlowcellDesignationByReagentBlockBarcode?reagentBlockBarcode=" + reagentKitBarcode)
  }

  def fetchLibraryDetailsByLibraryName(libraryNames: java.util.List[String]) = {
    val params = new MultivaluedMapImpl()
    libraryNames.foreach(params.add("q", _))
    val url = BASE_URL + "fetchLibraryDetailsByLibraryName"
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mLibraryCollectionType)
      case _ =>
        List()
    }
  }

  def fetchUserNameFromBadgeId(id: String) = {
    val url = BASE_URL + "fetchUserIdForBadgeId"
    val service = mClient.resource(url)
    val response = service.queryParam("badgeId", id).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        response.getEntity(classOf[String])
      case _ =>
        null
    }
  }

  def fetchQuantForTube(tubeBarcode: String, quantType: String) = {
    val url = BASE_URL + "fetchQuantForTube"
    val params = new MultivaluedMapImpl()
    params.add("tubeBarcode", tubeBarcode)
    params.add("quantType", quantType)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val string = response.getEntity(classOf[String])
        Some(string.toDouble)
      case _ =>
        None
    }
  }

  def fetchParentRackContentsForPlate(plateBarcode: String) = {
    val url = BASE_URL + "fetchParentRackContentsForPlate"
    val params = new MultivaluedMapImpl()
    params.add("plateBarcode", plateBarcode)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val fromJson = response.getEntity(classOf[String])
        val map : java.util.Map[String, Boolean] = mGson.fromJson(fromJson, mMapCollectionType)
        map
      case _ =>
        null
    }
  }

  def fetchQpcrForTube(tubeBarcode: String) = {
    val url = BASE_URL + "fetchQpcrForTube"
    val params = new MultivaluedMapImpl()
    params.add("tubeBarcode", tubeBarcode)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val string = response.getEntity(classOf[String])
        Some(string.toDouble)
      case _ =>
        None
    }
  }

  private def findFlowcellDesignation(url : String) : FlowcellDesignationType = {
    println(url)
    val service = mClient.resource(url)
    val response = service.get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mFlowcellDesignationType)
      case _ =>
        null
    }
  }

  def fetchTransfersForPlate(plateBarcode: String, depth: Int): util.List[PlateTransferType] = {
    val url = BASE_URL + "fetchTransfersForPlate"
    val params = new MultivaluedMapImpl()
    params.add("plateBarcode", plateBarcode)
    params.add("depth", depth)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mPlateTransfersType)
      case _ =>
        List()
    }
  }

  def findImmediatePlateParents(plateBarcode: String): util.List[String] = {
    val url = BASE_URL + "findImmediatePlateParents"
    val params = new MultivaluedMapImpl()
    params.add("plateBarcode", plateBarcode)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mStringCollectionType)
      case _ =>
        List()
    }
  }

  def fetchSourceTubesForPlate(plateBarcode: String): util.List[WellAndSourceTubeType] = {
    val url = BASE_URL + "fetchSourceTubesForPlate"
    val params = new MultivaluedMapImpl()
    params.add("plateBarcode", plateBarcode)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        mGson.fromJson(responseJson, mWellAndSourceTubeType)
      case _ =>
        List()
    }
  }

  def fetchIlluminaSeqTemplate(id: String, idType: SeqTypes.Value): SequencingTemplateType = {
    val url = BASE_URL + "fetchIlluminaSeqTemplate"
    val params = new MultivaluedMapImpl()
    params.add("id", id)
    params.add("idType", idType.toString)
    val service = mClient.resource(url)
    val response = service.queryParams(params).get(classOf[ClientResponse])
    response.getStatus match {
      case HttpURLConnection.HTTP_OK =>
        val responseJson = response.getEntity(classOf[String])
        val seqTemplate = (mGson.fromJson(responseJson, mSeqTemplate)).asInstanceOf[SequencingTemplateType]
        //Change read structure if TruSeq SS fast track
        //TODO remove when Mercury sets cycle information in the future
        if (seqTemplate != null) {
          if (seqTemplate.getProducts != null) {
            val filterByProduct = seqTemplate.getProducts.filter(_.name.equals(TRUSEQ_SS_FAST_TRACK_PRODUCT_NAME))
            if (filterByProduct.size == 1) {
              seqTemplate.setReadStructure("101T8B0B101T")
            }
          }
        }
        seqTemplate
      case _ =>
        null
    }
  }
}