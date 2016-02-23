package org.broadinstitute.gpinformatics.automation.controllers;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.User;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FingerprintNormalizationControllerTest {
    @Test
    public void testBuildFingerprintMessage() throws Exception {
        Rack sourceRack = new Rack();
        Rack destinationRack = new Rack();

        sourceRack.setBarcode("srcrackbarcode");
        destinationRack.setBarcode("destrackbarcode");

        /**
         * Source will lose 15uL but stay at 50 conc.
         */
        DilutionTransfer transfer = new DilutionTransfer();
        transfer.setSourceWell("A1");
        transfer.setSourceBarcode("srctube01");
        transfer.setDestinationWell("A1");
        transfer.setDestinationBarcode("desttube01");
        transfer.setSourceVolume(40);
        transfer.setSourceConcentration(50);
        transfer.setTargetVolume(30);
        transfer.setTargetConcentration(25);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        FingerprintNormalizationController controller = new FingerprintNormalizationController() {
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
        Reagent reagent = new Reagent("TE");
        reagent.setBarcode("ReagentBarcode");
        reagent.setExpirationDate(Calendar.getInstance());

        controller.protocolType = ProtocolTypes.FingerprintNormalization;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.reagent = reagent;
        controller.setSourceRack(sourceRack);
        controller.setDestinationRack(destinationRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();
        Assert.assertTrue(message.getPlateTransferEvent().size() == 1);
        PlateTransferEventType plateTransferEventType = message.getPlateTransferEvent().get(0);
        Assert.assertEquals(
                ProtocolTypes.FingerprintNormalization.getEventType(), plateTransferEventType.getEventType());
        Assert.assertTrue(plateTransferEventType.getReagent().size() == 1);

        ReagentType reagentType = plateTransferEventType.getReagent().get(0);
        Assert.assertEquals("TE", reagentType.getKitType());
        Assert.assertEquals("ReagentBarcode", reagentType.getBarcode());
        Assert.assertNotNull(reagentType.getExpiration());

        PlateType sourcePlate = plateTransferEventType.getSourcePlate();
        Assert.assertEquals("srcrackbarcode", sourcePlate.getBarcode());
        Assert.assertEquals("TubeRack", sourcePlate.getPhysType());
        Assert.assertEquals("ALL96", sourcePlate.getSection());

        PlateType plate = plateTransferEventType.getPlate();
        Assert.assertEquals("destrackbarcode", plate.getBarcode());
        Assert.assertEquals("TubeRack", plate.getPhysType());
        Assert.assertEquals("ALL96", plate.getSection());

        PositionMapType sourcePositionMap = plateTransferEventType.getSourcePositionMap();
        Assert.assertEquals("srcrackbarcode", sourcePositionMap.getBarcode());
        List<ReceptacleType> sourcePositionMapReceptacles = sourcePositionMap.getReceptacle();
        Assert.assertTrue(sourcePositionMapReceptacles.size() == 1);
        ReceptacleType sourceReceptacleType = sourcePositionMapReceptacles.get(0);
        Assert.assertEquals("srctube01",sourceReceptacleType.getBarcode());
        Assert.assertEquals("A1",sourceReceptacleType.getPosition());
        Assert.assertNull(sourceReceptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(2500, 2), sourceReceptacleType.getVolume());

        PositionMapType positionMap = plateTransferEventType.getPositionMap();
        Assert.assertEquals("destrackbarcode", positionMap.getBarcode());
        List<ReceptacleType> positionMapReceptacles = positionMap.getReceptacle();
        Assert.assertTrue(positionMapReceptacles.size() == 1);
        ReceptacleType receptacleType = positionMapReceptacles.get(0);
        Assert.assertEquals("desttube01",receptacleType.getBarcode());
        Assert.assertEquals("A1",receptacleType.getPosition());
        Assert.assertEquals(BigDecimal.valueOf(2500, 2), receptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(3000, 2), receptacleType.getVolume());
    }
}
