package org.broadinstitute.techdev.lims.mercury

import org.junit.Assert
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import scala.collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers

/**
 * User: John Walsh
 * Date: 8/14/12
 */

@RunWith(classOf[JUnitRunner])
class LimsServiceImplTest extends FunSuite with BeforeAndAfter with ShouldMatchers{
  private var mClient : LimsService = _

  before{
    mClient = new LimsServiceMercuryImpl()
  }

  test("check if flowcell gives any insight to the reagent kit"){
    mClient = new LimsServiceMercuryImpl()
    val transfers = mClient.fetchIlluminaSeqTemplate("ABUTW", SeqTypes.flowcell)
    print(transfers)
  }

  test("fetch quant for tube 0171709856") {
    mClient = new LimsServiceMercuryImpl("gplims.broadinstitute.org", "http")
    val quant = mClient.fetchQuantForTube("0171709856", "Pond Pico")
    println(quant)
  }

  test("fetchLibraryDetailsByTubeBarcode works on 0116261544"){
    mClient = new LimsServiceMercuryImpl("mercury.broadinstitute.org:8443", "https")
    val libraries = mClient.fetchLibraryDetailsByTubeBarcode(List("HAB32ADXX"), includeWorkRequestDetails = true)
    libraries.foreach(library => println(library.getLibraryName()))
    assert(!libraries.isEmpty())
  }

  test("doesLimsRecognizeAllTubes works on 0116261544"){
    val doesLimLikeMe = mClient.doesLimsRecognizeAllTubes(List("0116261544"))
    assert(doesLimLikeMe)
  }

  test("doesLimsRecognizeAllTubes fails on 0116261544 and 0116261544555"){
    val doesLimsLikeMe = mClient.doesLimsRecognizeAllTubes(List("0116261544", "0116261544555"))
    assert(!doesLimsLikeMe)
  }

  test("findFlowcellDesignationByTaskName works with 9A_08.05.2012"){
    val fd = mClient.findFlowcellDesignationByTaskName("9A_08.05.2012")
    assert(fd != null)
  }

  test("findFlowcellDesignationByTaskName returns null with 9A_08.05.2012_UNKNOWN"){
    val fd = mClient.findFlowcellDesignationByTaskName("9A_08.05.2012_UNKNOWN")
    assert(fd == null)
  }

  test("findFlowcellDesignationByFlowcellBarcode works with C0W2BACXX"){
    val fd = mClient.findFlowcellDesignationByFlowcellBarcode("C0W2BACXX")
    assert(fd != null)
    assert(fd.getDesignationName equals "9A_08.05.2012")
    expect(true){
      fd.isIndexedRun
    }
  }

  test("findFlowcellDesignationByFlowcellBarcode works with D1MPKACXX"){
    val fd = mClient.findFlowcellDesignationByFlowcellBarcode("D1MPKACXX")
    assert(fd != null)
  }

  test("findFlowcellDesignationByReagentBlockBarcode works with MS2011538-00300"){
    val fd = mClient.findFlowcellDesignationByReagentBlockBarcode("MS2011538-00300")
    //assert(fd != null)        //TODO this isn't implemented yet
    //assert(fd.getDesignationName equals "SOMETHING")
  }

  test("fetchLibraryDetailsByLibraryName works with Solexa-133183"){
    val fd = mClient.fetchLibraryDetailsByLibraryName(List("Solexa-133183"))
    assert(fd != null)
  }

  test("fetchUserNameFromBadgeId works with 79fb7000f6ff12e0 in mercury bsp"){
    val client = new LimsServiceMercuryImpl("seqlims")
    client.setBaseUrl("http://mercurydev.broadinstitute.org:8080/Mercury/rest/limsQuery/")
    expect("emsalls"){
      client.fetchUserNameFromBadgeId("79fb7000f6ff12e0")
    }
  }

  test("fetchUserNameFromBadgeId works with 79fb7000f6ff12e0 in mercury bsp with wrong username"){
    val client = new LimsServiceMercuryImpl("seqlims")
    client.setBaseUrl("http://mercurydev.broadinstitute.org:8080/Mercury/rest/limsQuery/")
    val username = client.fetchUserNameFromBadgeId("79fb7000f6ff12e0")
    username should not equal "emsalls!!!"
  }

  test("fetchQuantForTube works with 0116261544"){
    val fd = mClient.fetchQuantForTube("0116261544", "Pond Pico")
    assert(fd != null)
  }

  test("fetchParentRackContentsForPlate works with 000000146452"){
    val rackLayout = mClient.fetchParentRackContentsForPlate("000000146452") //TODO Get Plate in Production
    assert(rackLayout != null)
  }

  test("fetchQpcrForTube returns None with 0116261544"){
    expect(None) {
      mClient.fetchQpcrForTube("0116261544")
    }
  }

  test("fetchTransfersForPlate is not null for 000008160873"){
    val transfers = mClient.fetchTransfersForPlate("000008160873",1)
    assert(transfers != null)
    assert(!transfers.isEmpty)
  }

  test("2 lane flowcell"){
    val fd = mClient.findFlowcellDesignationByTaskName("3A_03.14.2013")
    assert(fd != null)
  }

  test("IgFit product read structure") {
    val seqTemplate = mClient.fetchIlluminaSeqTemplate("C8GAYANXX", SeqTypes.flowcell)
    val actualReadStruct = seqTemplate.getReadStructure()
    val expectedReadStructure = "101T8B0B101T"
    Assert.assertEquals(expectedReadStructure, actualReadStruct)
  }
}
