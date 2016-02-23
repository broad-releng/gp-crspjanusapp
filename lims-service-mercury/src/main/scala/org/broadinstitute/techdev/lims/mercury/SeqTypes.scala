package org.broadinstitute.techdev.lims.mercury

/**
 * User: jowalsh
 * Date: 6/26/13
 */
object SeqTypes extends Enumeration{
  val flowcell = Value("FLOWCELL")
  val miseqReagentKit = Value("MISEQ_REAGENT_KIT")
  val dilutionTube = Value("TUBE")
  val flowcellTrackingTicket = Value("FLOWCELL_TICKET")
}
