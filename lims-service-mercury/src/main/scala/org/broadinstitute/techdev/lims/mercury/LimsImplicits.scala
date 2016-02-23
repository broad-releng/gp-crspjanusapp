package org.broadinstitute.techdev.lims.mercury

import org.broadinstitute.techdev.lims.mercury.SequencingTemplateType

/**
  * User: jowalsh
  * Date: 6/26/13
  * 76T8B8B76T
  */
object LimsImplicits {
  implicit class SequencingTemplateTypeExtensions(sequencingTemplate : SequencingTemplateType) {
    private val templatePattern = """(\d+)T""".r
    private val indexPattern = """(\d*)T*(\d+)B+(.*)T*""".r
    def numberOfIndexReads = sequencingTemplate.getReadStructure.count(_ == 'B')
    def numberOfTemplateReads = sequencingTemplate.getReadStructure.count(_ == 'T')

    def templateReadLength = {
      val rl = templatePattern findPrefixOf sequencingTemplate.getReadStructure getOrElse "0T"
      val removeTemplateTag = rl.replaceAll("T", "")
      removeTemplateTag.toInt
    }

    def indexReadLength = {
      sequencingTemplate.getReadStructure match {
        case indexPattern(template,index, _) => {
          index.toInt
        }
        case _ => 0
      }
    }

    def index2ReadLength : Int = {
      if (!isDualIndexedRun)
        0
      else {
        val re = """(\d+)B""".r
        val indexReads = re.findAllIn(sequencingTemplate.getReadStructure).matchData.toList
        if (indexReads.size == 2) {
          indexReads(1).group(1).toInt
        }
        else
          0
      }
    }

    def isIndexedRun = numberOfIndexReads > 0
    def isPairedEnd = numberOfTemplateReads == 2
    def isDualIndexedRun = numberOfIndexReads == 2
  }
}