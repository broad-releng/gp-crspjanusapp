package org.broadinstitute.gpinformatics.automation.controllers;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.VolumeTransfer;
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

public class VolumeTransferControllerTest {

    @Test
    public void testBuildVolumeTransferMessage() throws Exception {
        Rack sourceRack = new Rack();
        Rack destinationRack = new Rack();

        sourceRack.setBarcode("srcrackbarcode");
        destinationRack.setBarcode("destrackbarcode");

        /**
         * Source will lose 15uL but stay at 50 conc.
         */
        VolumeTransfer transfer = new VolumeTransfer();
        transfer.setSourceWell("A1");
        transfer.setSourceBarcode("srctube01");
        transfer.setDestinationWell("A1");
        transfer.setDestinationBarcode("desttube01");
        transfer.setSourceVolume(40);
        transfer.setSourceConcentration(10);
        transfer.setDestinationVolume(20);
        transfer.setDestinationConcentration(10);
        transfer.setNewSourceVolume(20);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        VolumeTransferController controller = new VolumeTransferController() {
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

        controller.protocolType = ProtocolTypes.VolumeTransfer;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.setSourceRack(sourceRack);
        controller.setDestinationRack(destinationRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();

        //Asserts
        Assert.assertTrue(message.getPlateTransferEvent().size() == 1);
        PlateTransferEventType plateTransferEventType = message.getPlateTransferEvent().get(0);
        Assert.assertEquals(
                ProtocolTypes.VolumeTransfer.getEventType(), plateTransferEventType.getEventType());
        Assert.assertTrue(plateTransferEventType.getReagent().size() == 0);

        PlateType sourcePlate = plateTransferEventType.getSourcePlate();
        Assert.assertEquals("srcrackbarcode", sourcePlate.getBarcode());
        Assert.assertEquals("TubeRack", sourcePlate.getPhysType());
        Assert.assertEquals("ALL96", sourcePlate.getSection());

        PlateType plate = plateTransferEventType.getPlate();
        Assert.assertEquals("destrackbarcode", plate.getBarcode());
        Assert.assertEquals("TubeRack", plate.getPhysType());
        Assert.assertEquals("ALL96", plate.getSection());

        //Assert Volume removed from source
        PositionMapType sourcePositionMap = plateTransferEventType.getSourcePositionMap();
        Assert.assertEquals("srcrackbarcode", sourcePositionMap.getBarcode());
        List<ReceptacleType> sourcePositionMapReceptacles = sourcePositionMap.getReceptacle();
        Assert.assertTrue(sourcePositionMapReceptacles.size() == 1);
        ReceptacleType sourceReceptacleType = sourcePositionMapReceptacles.get(0);
        Assert.assertEquals("srctube01",sourceReceptacleType.getBarcode());
        Assert.assertEquals("A1",sourceReceptacleType.getPosition());
        Assert.assertNull(sourceReceptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(2000, 2), sourceReceptacleType.getVolume());

        //Assert correct v+c in destination
        PositionMapType positionMap = plateTransferEventType.getPositionMap();
        Assert.assertEquals("destrackbarcode", positionMap.getBarcode());
        List<ReceptacleType> positionMapReceptacles = positionMap.getReceptacle();
        Assert.assertTrue(positionMapReceptacles.size() == 1);
        ReceptacleType receptacleType = positionMapReceptacles.get(0);
        Assert.assertEquals("desttube01",receptacleType.getBarcode());
        Assert.assertEquals("A1",receptacleType.getPosition());
        Assert.assertEquals(BigDecimal.valueOf(1000, 2), receptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(2000, 2), receptacleType.getVolume());
    }
}
