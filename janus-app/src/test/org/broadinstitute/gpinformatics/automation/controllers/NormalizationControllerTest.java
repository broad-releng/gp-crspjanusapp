package org.broadinstitute.gpinformatics.automation.controllers;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateEventType;
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

public class NormalizationControllerTest {
    @Test
    public void testBuildNormMessage() throws Exception {
        Rack sourceRack = new Rack();

        sourceRack.setBarcode("srcrackbarcode");

        /**
         * Cutting in half will result in twice as much volume
         * expet conc = 25, vol = 80
         */
        Normalization transfer = new Normalization(true, false);
        transfer.setWell("A1");
        transfer.setBarcode("srctube01");
        transfer.setV1(40);
        transfer.setC1(50);
        transfer.setTarget(25);
        final List<WorklistRow> transfers = new ArrayList<>();
        transfers.add(transfer);

        Normalization transfer2 = new Normalization(true, false);
        transfer2.setWell("A2");
        transfer2.setBarcode("srctube02");
        transfer2.setV1(50);
        transfer2.setC1(200);
        transfer2.setTarget(25);
        transfers.add(transfer2);

        NormalizationController controller = new NormalizationController() {
            @Override
            public WorklistBuilder doOnInitialized() {
                return null;
            }

            @Override
            public List<WorklistRow> getWorklist() {
                return transfers;
            }
        };
        MachineNameLookup machineNameLookup = mock(MachineNameLookup.class);
        when(machineNameLookup.getMachineName()).thenReturn("BATMAN");

        Reagent reagent = new Reagent("TE");
        reagent.setBarcode("ReagentBarcode");
        reagent.setExpirationDate(Calendar.getInstance());

        controller.protocolType = ProtocolTypes.Normalization;
        controller.user = new User("jowalsh");
        controller.machineNameLookup = machineNameLookup;
        controller.reagent = reagent;
        controller.setRack(sourceRack);

        BettaLIMSMessage message = controller.buildBettalimsMessage();
        Assert.assertTrue(message.getPlateEvent().size() == 1);
        PlateEventType plateEventType = message.getPlateEvent().get(0);
        Assert.assertEquals(
                ProtocolTypes.Normalization.getEventType(), plateEventType.getEventType());

        PlateType plate = plateEventType.getPlate();
        Assert.assertEquals("srcrackbarcode", plate.getBarcode());
        Assert.assertEquals("TubeRack", plate.getPhysType());
        Assert.assertEquals("ALL96", plate.getSection());

        ReagentType reagentType = plateEventType.getReagent().get(0);
        Assert.assertEquals("TE", reagentType.getKitType());
        Assert.assertEquals("ReagentBarcode", reagentType.getBarcode());
        Assert.assertNotNull(reagentType.getExpiration());

        PositionMapType positionMap = plateEventType.getPositionMap();
        Assert.assertEquals("srcrackbarcode", positionMap.getBarcode());
        List<ReceptacleType> sourcePositionMapReceptacles = positionMap.getReceptacle();
        Assert.assertTrue(sourcePositionMapReceptacles.size() == 2);
        ReceptacleType sourceReceptacleType = sourcePositionMapReceptacles.get(0);
        Assert.assertEquals("srctube01",sourceReceptacleType.getBarcode());
        Assert.assertEquals("A1", sourceReceptacleType.getPosition());
        Assert.assertEquals(BigDecimal.valueOf(2500, 2), sourceReceptacleType.getConcentration());
        Assert.assertEquals(BigDecimal.valueOf(8000, 2), sourceReceptacleType.getVolume());

        // Should split overflow of tips 200 into two rows
        List<WorklistRow> splitTransfers = controller.splitTransfers(transfers);
        Assert.assertEquals(3, splitTransfers.size());
    }
}
