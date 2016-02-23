package org.broadinstitute.techdev.lims.mercury

import org.broadinstitute.techdev.lims.mercury._

import scala.collection.JavaConverters._

/**
  * Client resource to talk to Mercury
  */

trait LimsService {

  def findImmediatePlateParents(plateBarcode : String): java.util.List[String]

  def fetchLibraryDetailsByTubeBarcode(tubeBarcodes: java.util.List[String],
                                       includeWorkRequestDetails: Boolean): java.util.List[LibraryDataType]

  def doesLimsRecognizeAllTubes(tubeBarcodes: Iterable[String]): Boolean

  def doesLimsRecognizeAllTubes(tubeBarcodes: java.util.List[String]): Boolean = {
    doesLimsRecognizeAllTubes(tubeBarcodes.asScala)
  }

  def findFlowcellDesignationByTaskName(taskName: String): FlowcellDesignationType

  def fetchUnfulfilledDesignations: List[String]

  def findFlowcellDesignationByFlowcellBarcode(flowcellBarcode: String): FlowcellDesignationType

  def findFlowcellDesignationByReagentBlockBarcode(reagentKitBarcode: String): FlowcellDesignationType

  def fetchLibraryDetailsByLibraryName(libraryNames: java.util.List[String]): java.util.List[LibraryDataType]

  def fetchUserNameFromBadgeId(id: String): String

  def fetchQuantForTube(tubeBarcode: String, quantType: String): Option[java.lang.Double]

  def fetchQpcrForTube(tubeBarcode: String): Option[java.lang.Double]

  def fetchParentRackContentsForPlate(plateBarcode: String): java.util.Map[String, Boolean]

  def fetchTransfersForPlate(plateBarcode: String, depth: Int) : java.util.List[PlateTransferType]

  def fetchSourceTubesForPlate(plateBarcode : String) : java.util.List[WellAndSourceTubeType]

  def fetchIlluminaSeqTemplate(id : String, idType: SeqTypes.Value) : SequencingTemplateType

  def fetchConcentrationAndVolumeAndWeightForTubeBarcodes(barcodes : java.util.List[String]) : java.util.Map[String, ConcentrationAndVolumeAndWeightType]

  def sslInit()
}

class LimsServiceFactoryMercury(hostname : String, protocol : String = "http") {
  def newService = new LimsServiceMercuryImpl("mercury.broadinstitute.org:8443", "https")
}
