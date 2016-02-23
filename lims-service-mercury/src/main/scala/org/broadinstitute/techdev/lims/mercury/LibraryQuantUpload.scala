package org.broadinstitute.techdev.lims.mercury

import com.sun.jersey.api.client.{ClientResponse, Client}
import javax.ws.rs.core.MediaType

/**
  * User: John Walsh
  * Date: 11/8/12
  */
class LibraryQuantUpload{
  private val mClient = new Client()

  def upload(xml : String) : Boolean = {
    val url = "http://seqlims02.broadinstitute.org:9998/libraryquant"
    val service = mClient.resource(url)
    val response = service.`type`(MediaType.APPLICATION_XML).post(classOf[ClientResponse], xml)
    println("LibraryQuantUpload: " + response.getStatus)
    response.getStatus == 200
  }
}