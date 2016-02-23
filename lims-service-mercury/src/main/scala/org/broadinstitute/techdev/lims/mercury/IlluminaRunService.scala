package org.broadinstitute.techdev.lims.mercury

import org.broadinstitute.techdev.lims.mercury.{ZimsIlluminaRunType, SolexaRunBean}

/**
  * Defines Illumina Run Service methods
  */
trait IlluminaRunService {
  def createRun(solexaRunBean : SolexaRunBean) : Boolean

  def queryRun(runName : String) : ZimsIlluminaRunType
}
