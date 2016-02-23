package org.broadinstitute.gpinformatics.automation.controllers;

import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.TransferType;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReceptacleType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TruSeqSpikeControllerTest {

    MachineNameLookup machineNameLookup = mock(MachineNameLookup.class);

    @Before
    public void setUp() throws Exception {
        when(machineNameLookup.getMachineName()).thenReturn("BATMAN");
    }


    @Test
    public void testBuildTruSeqSpikeMessage() throws Exception {
        Rack parentRack = new Rack();
        Rack daughterRack = new Rack();

        parentRack.setBarcode("parentrack");
        daughterRack.setBarcode("daughterrack");

        /**
         * Parent will lose 15uL but stay at 50 conc.
         */
        NormalizationAndSpikeRow transfer = new NormalizationAndSpikeRow(5, 10);
        transfer.setDaughterWell("A1");
        transfer.setDaughterBarcode("Daughter01");
        transfer.setParentWell("A1");
        transfer.setParentBarcode("parent01");
        transfer.setNewDaughterVolume(40);
        transfer.setNewDaughterConcentration(5);
        transfer.setNewParentVolume(30);
        transfer.setParentConcentration(100);
        transfer.setTransferType(TransferType.SPIKE);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        TruSeqSpikeController controller = new TruSeqSpikeController() {
            @Override
            public WorklistBuilder doOnInitialized() {
                return null;
            }

            @Override
            public List<WorklistRow> getWorklist() {
                return transfers;
            }
        };

        //Set reagents
        Reagent reagent = new Reagent("EB");
        reagent.setBarcode("ReagentBarcode");
        reagent.setExpirationDate(Calendar.getInstance());

        controller.protocolType = ProtocolTypes.TruSeqSpike;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.reagent = reagent;
        controller.setSourceRack(parentRack);
        controller.setDestinationRack(daughterRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();
        assertEquals(2, message.getPlateEvent().size());

        //Asserts - Daughter Plates
        PlateEventType daughterPlateEvent = message.getPlateEvent().get(0);
        assertEquals(ProtocolTypes.TruSeqSpike.getEventType(), daughterPlateEvent.getEventType());
        assertEquals("daughterrack", daughterPlateEvent.getPlate().getBarcode());
        assertEquals("daughterrack", daughterPlateEvent.getPositionMap().getBarcode());
        ReceptacleType daughterReceptacle = daughterPlateEvent.getPositionMap().getReceptacle().get(0);
        assertEquals("Daughter01", daughterReceptacle.getBarcode());
        assertEquals(new BigDecimal("5.00"), daughterReceptacle.getConcentration());
        assertEquals(new BigDecimal("40.00"), daughterReceptacle.getVolume());

        //Asserts - Parent Plates
        PlateEventType parentPlateEvent = message.getPlateEvent().get(1);
        assertEquals(ProtocolTypes.TruSeqSpike.getEventType(), parentPlateEvent.getEventType());
        assertEquals("parentrack", parentPlateEvent.getPlate().getBarcode());

        ReceptacleType parentReceptacle = parentPlateEvent.getPositionMap().getReceptacle().get(0);
        assertEquals("parent01", parentReceptacle.getBarcode());
        assertEquals(new BigDecimal("100.00"), parentReceptacle.getConcentration());
        assertEquals(new BigDecimal("30.00"), parentReceptacle.getVolume());
    }

    @Test
    public void testBuildTruSeqNormOnlyMessage() throws Exception {
        Rack daughterRack = new Rack();

        daughterRack.setBarcode("daughterrack");

        NormalizationAndSpikeRow transfer = new NormalizationAndSpikeRow(5, 10);
        transfer.setDaughterWell("A1");
        transfer.setDaughterBarcode("Daughter01");
        transfer.setParentWell("A1");
        transfer.setParentBarcode("parent01");
        transfer.setDaughterVolume(20);
        transfer.setDaughterConcentration(15);
        transfer.setNewDaughterVolume(40);
        transfer.setNewDaughterConcentration(10);
        transfer.setTransferType(TransferType.NORMALIZATION);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        TruSeqSpikeController controller = new TruSeqSpikeController() {
            @Override
            public WorklistBuilder doOnInitialized() {
                return null;
            }

            @Override
            public List<WorklistRow> getWorklist() {
                return transfers;
            }
        };

        //Set reagents
        Reagent reagent = new Reagent("EB");
        reagent.setBarcode("ReagentBarcode");
        reagent.setExpirationDate(Calendar.getInstance());

        controller.protocolType = ProtocolTypes.TruSeqSpike;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.reagent = reagent;
        controller.setDestinationRack(daughterRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();
        assertEquals(1, message.getPlateEvent().size());

        //Asserts - Daughter Plates
        PlateEventType daughterPlateEvent = message.getPlateEvent().get(0);
        assertEquals(ProtocolTypes.TruSeqSpike.getEventType(), daughterPlateEvent.getEventType());
        assertEquals("daughterrack", daughterPlateEvent.getPlate().getBarcode());
        assertEquals("daughterrack", daughterPlateEvent.getPositionMap().getBarcode());
        ReceptacleType daughterReceptacle = daughterPlateEvent.getPositionMap().getReceptacle().get(0);
        assertEquals("Daughter01", daughterReceptacle.getBarcode());
        assertEquals(new BigDecimal("10.00"), daughterReceptacle.getConcentration());
        assertEquals(new BigDecimal("40.00"), daughterReceptacle.getVolume());
    }
}