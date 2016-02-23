package org.broadinstitute.gpinformatics.automation.controllers;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WellTransferRow;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateTransferEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReagentType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReceptacleType;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.WeakHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RNACaliperQCControllerTest {
    @Test
    public void testBuildRNACaliperQCMessage() throws Exception {
        Rack sourceRack = new Rack();
        Plate destinationPlate = new Plate();

        sourceRack.setBarcode("srcrackbarcode");
        destinationPlate.setBarcode("destrackbarcode");

        /**
         * Source will lose 1uL but stay at 50 conc. Dest will be no
         */
        WellTransferRow transfer = new WellTransferRow();
        transfer.setSourceWell("A1");
        transfer.setSourceBarcode("srctube01");
        transfer.setDestinationWell("A1");
        transfer.setSourceVolume(40);
        transfer.setSourceConcentration(50);
        transfer.setTargetConcentration(5);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        RNACaliperQCController controller = new RNACaliperQCController() {
            @Override
            public WorklistBuilder doOnInitialized() {
                return null;
            }

            @Override
            public List<WorklistRow> getWorklist() {
                return transfers;
            }
        };
        //Set machine name
        MachineNameLookup machineNameLookup = mock(MachineNameLookup.class);
        when(machineNameLookup.getMachineName()).thenReturn("BATMAN");

        //Set reagents
        Reagent reagent = new Reagent("Sample Buffer");
        reagent.setBarcode("ReagentBarcode");
        reagent.setExpirationDate(Calendar.getInstance());

        controller.protocolType = ProtocolTypes.RNACaliperQC;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.reagent = reagent;
        controller.setSourceRack(sourceRack);
        controller.setDestinationPlate(destinationPlate);

        BettaLIMSMessage message = controller.buildBettalimsMessage();

        //Asserts
        Assert.assertTrue(message.getPlateTransferEvent().size() == 1);
        PlateTransferEventType plateTransferEventType = message.getPlateTransferEvent().get(0);
        Assert.assertEquals(
                ProtocolTypes.RNACaliperQC.getEventType(), plateTransferEventType.getEventType());
        Assert.assertTrue(plateTransferEventType.getReagent().size() == 1);

        ReagentType reagentType = plateTransferEventType.getReagent().get(0);
        Assert.assertEquals("Sample Buffer", reagentType.getKitType());
        Assert.assertEquals("ReagentBarcode", reagentType.getBarcode());
        Assert.assertNotNull(reagentType.getExpiration());

        PlateType sourcePlate = plateTransferEventType.getSourcePlate();
        Assert.assertEquals("srcrackbarcode", sourcePlate.getBarcode());
        Assert.assertEquals("TubeRack", sourcePlate.getPhysType());
        Assert.assertEquals("ALL96", sourcePlate.getSection());

        PlateType plate = plateTransferEventType.getPlate();
        Assert.assertEquals("destrackbarcode", plate.getBarcode());
        Assert.assertEquals("Eppendorf384", plate.getPhysType());
        Assert.assertEquals("P384_96TIP_1INTERVAL_A1", plate.getSection());

        //Assert Volume removed from source
        PositionMapType sourcePositionMap = plateTransferEventType.getSourcePositionMap();
        Assert.assertEquals("srcrackbarcode", sourcePositionMap.getBarcode());
        List<ReceptacleType> sourcePositionMapReceptacles = sourcePositionMap.getReceptacle();
        Assert.assertTrue(sourcePositionMapReceptacles.size() == 1);
        ReceptacleType sourceReceptacleType = sourcePositionMapReceptacles.get(0);
        Assert.assertEquals("srctube01",sourceReceptacleType.getBarcode());
        Assert.assertEquals("A01",sourceReceptacleType.getPosition());
        Assert.assertEquals(BigDecimal.valueOf(5000, 2), sourceReceptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(3900, 2), sourceReceptacleType.getVolume());

        //Assert correct v+c in destination
        PositionMapType positionMap = plateTransferEventType.getPositionMap();
        Assert.assertEquals("destrackbarcode", positionMap.getBarcode());
        List<ReceptacleType> positionMapReceptacles = positionMap.getReceptacle();
        Assert.assertTrue(positionMapReceptacles.size() == 1);
        ReceptacleType receptacleType = positionMapReceptacles.get(0);
        Assert.assertEquals("A01",receptacleType.getPosition());
        Assert.assertEquals(BigDecimal.valueOf(500, 2), receptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(1000, 2), receptacleType.getVolume());
    }
}