package org.broadinstitute.techdev.lims.mercury

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.util.GregorianCalendar
import javax.xml.datatype.{XMLGregorianCalendar, DatatypeFactory}
import scala.collection.JavaConversions._

/**
 * Test client resource for registering/view illumina runs
 */
@RunWith(classOf[JUnitRunner])
class IlluminaRunServiceTest extends FunSuite with BeforeAndAfter with ShouldMatchers {
  private var mClient : IlluminaRunService = _

  before{
    mClient = new IlluminaRunServiceImpl("mercurydev.broadinstitute.org:8443")
  }

  test("Registered Runs should not be able to be re-registered: 141008_SL-HDJ_0452_AHAL8KADXX") {
    val solexaRunBean = new SolexaRunBean()
    solexaRunBean.setFlowcellBarcode("HAL8KADXX")
    solexaRunBean.setMachineName("SL-HDJ")
    solexaRunBean.setRunBarcode("HAL8KADXX141008")
    solexaRunBean.setRunDirectory("/seq/illumina/proc/SL-HDJ/141008_SL-HDJ_0452_AHAL8KADXX")
    solexaRunBean.setRunDate(getDate)
    solexaRunBean.setReagentBlockBarcode("")
    val result = mClient.createRun(solexaRunBean)
    assert(!result)
  }

  test("createRun for unknown flowcell should not work"){
    val run = new SolexaRunBean
    run.setFlowcellBarcode("nonesensefbar")
    run.setMachineName("zanzibar")
    val now = getDate
    run.setRunDate(now)
    run.setRunDirectory("rundir")
    var result = mClient.createRun(run)
    assert(!result)
  }

  test("querying 141008_SL-HDJ_0452_AHAL8KADXX should reveal IRB") {
    val run = mClient.queryRun("141008_SL-HDJ_0452_AHAL8KADXX")
    run should not be null
    val lanes = run.lanes
    assert(!lanes.isEmpty)
    val lane = lanes(0)
    val library = lane.libraries(0)
    val designation = library.regulatoryDesignation
    assert(designation.equals("RESEARCH_ONLY"))
    assert(library.researchProjectId.equals("RP-808"))
    assert(library.researchProjectName.equals("TiN testing"))
  }

  test("querying buick validation") {
    val run = mClient.queryRun("141031_SL-HCD_0319_AFCHAHFBADXX")
    run should not be null
    val lanes = run.lanes
    assert(!lanes.isEmpty)
    val lane = lanes(0)
    val library = lane.libraries(0)
    val designation = library.regulatoryDesignation
    assert(designation.equals("GENERAL_CLIA_CAP"))
    assert(library.researchProjectId.equals("RP-830"))
    assert(library.researchProjectName.equals("Takeda Hem-onc v2 Custom Hybrid Selection Panel Process Validation"))
  }

  private def getDate: XMLGregorianCalendar = {
    try {
      val gregorianCalendar = new GregorianCalendar()
      val datatypeFactory = DatatypeFactory.newInstance()
      datatypeFactory.newXMLGregorianCalendar(gregorianCalendar)
    } catch {
      case e: Exception => fail("Couldn't retrieve date", e)
    }
  }

}
