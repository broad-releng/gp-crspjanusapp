package org.broadinstitute.gpinformatics.automation.controllers;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.worklist.AllSampleTransferWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateTransferEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReceptacleType;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SonicDaughterPlateCreationControllerTest {
    @Test
    public void testBuildTruSeqAliquotMessage() throws Exception {
        Rack sourceRack = new Rack();
        Rack destinationRack = new Rack();

        sourceRack.setBarcode("srcrackbarcode");
        destinationRack.setBarcode("destrackbarcode");

        sourceRack.addTube(0, 0, "SrcTube");
        destinationRack.addTube(0, 0, "DestTube");

        LimsService limsService = mock(LimsService.class);

        ConcentrationAndVolumeAndWeightType concentrationAndVolumeAndWeightType = new ConcentrationAndVolumeAndWeightType();
        concentrationAndVolumeAndWeightType.setTubeBarcode("SrcTube");
        concentrationAndVolumeAndWeightType.setVolume(150);
        Map<String, ConcentrationAndVolumeAndWeightType> map = new HashMap<>();
        map.put("SrcTube", concentrationAndVolumeAndWeightType);
        List<String> tubeBarcodes = new ArrayList<>();
        tubeBarcodes.add("SrcTube");
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(tubeBarcodes)).thenReturn(map);

        final AllSampleTransferWorklistBuilder allSampleTransferWorklistBuilder =
                new AllSampleTransferWorklistBuilder(sourceRack, destinationRack, limsService);

        SonicDaughterPlateCreationController controller = new SonicDaughterPlateCreationController() {
            @Override
            public WorklistBuilder doOnInitialized() {
                return null;
            }

            @Override
            public List<WorklistRow> getWorklist() {
                return allSampleTransferWorklistBuilder.build();
            }
        };
        //Set machine name
        MachineNameLookup machineNameLookup = mock(MachineNameLookup.class);
        when(machineNameLookup.getMachineName()).thenReturn("BATMAN");

        controller.protocolType = ProtocolTypes.SonicDaughterPlateCreation;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.setSourceRack(sourceRack);
        controller.setDestinationRack(destinationRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();

        //Asserts
        Assert.assertTrue(message.getPlateTransferEvent().size() == 1);
        PlateTransferEventType plateTransferEventType = message.getPlateTransferEvent().get(0);
        Assert.assertEquals("SonicDaughterPlateCreation", plateTransferEventType.getEventType());

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
        Assert.assertEquals("SrcTube",sourceReceptacleType.getBarcode());
        Assert.assertEquals("A1",sourceReceptacleType.getPosition());
        Assert.assertNull(sourceReceptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(0, 2), sourceReceptacleType.getVolume());

        //Assert correct v+c in destination
        PositionMapType positionMap = plateTransferEventType.getPositionMap();
        Assert.assertEquals("destrackbarcode", positionMap.getBarcode());
        List<ReceptacleType> positionMapReceptacles = positionMap.getReceptacle();
        Assert.assertTrue(positionMapReceptacles.size() == 1);
        ReceptacleType receptacleType = positionMapReceptacles.get(0);
        Assert.assertEquals("DestTube",receptacleType.getBarcode());
        Assert.assertEquals("A1",receptacleType.getPosition());
        //Target Concentration should be 2 decimal places rounded up instead of: 26.66897
        Assert.assertNull(receptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(15000, 2), receptacleType.getVolume());
    }
}