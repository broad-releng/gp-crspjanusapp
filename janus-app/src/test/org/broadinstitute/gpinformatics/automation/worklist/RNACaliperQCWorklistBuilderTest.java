package org.broadinstitute.gpinformatics.automation.worklist;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.WellTransferRow;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RNACaliperQCWorklistBuilderTest {
    private LimsService limsService;
    private Rack source;
    private Plate destination;

    @Before
    public void setUp() throws Exception {
        source = new Rack();
        destination = new Plate();
    }

    @Test
    public void testSuccessfulBuild() throws Exception {
        double V1 = 20;
        double C1 = 60;
        double C2 = 5;

        String sourceTubebarcode = "0116404297";
        List<String> sourceTubes = Arrays.asList(sourceTubebarcode);

        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType ConcentrationAndVolumeAndWeightType = new ConcentrationAndVolumeAndWeightType();
        ConcentrationAndVolumeAndWeightType.setVolume(V1);
        ConcentrationAndVolumeAndWeightType.setConcentration(C1);
        ConcentrationAndVolumeAndWeightType.setTubeBarcode(sourceTubebarcode);
        concentrationAndVolumeAndWeightTypes.put(sourceTubebarcode, ConcentrationAndVolumeAndWeightType);

        limsService = mock(LimsService.class);
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(sourceTubes))
                .thenReturn(concentrationAndVolumeAndWeightTypes);
        when(limsService.doesLimsRecognizeAllTubes(sourceTubes))
                .thenReturn(true);

        TargetConcentration targetVolume =
                new TargetConcentration();
        targetVolume.setTargetConcentration(String.valueOf(C2));
        RNACaliperQCWorklistBuilder builder =
                new RNACaliperQCWorklistBuilder(source, destination, targetVolume, limsService);
        source.addTube(0, 1, sourceTubebarcode); //A2 gets mapped to A3
        source.setBarcode("srcbarcode");
        destination.setBarcode("destbarcode");
        List<WorklistRow> rows = builder.build();
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() == 1);

        WellTransferRow transfer = (WellTransferRow) rows.get(0);
        Assert.assertEquals(sourceTubebarcode, transfer.getSourceBarcode());
        Assert.assertEquals("A02", transfer.getSourceWell());
        Assert.assertEquals(19.0, transfer.getNewSourceVolume());
        Assert.assertEquals(11.0, transfer.getTe());
        Assert.assertEquals(12.0, transfer.getDestinationVolume());
        Assert.assertEquals("A03", transfer.getDestinationWell());
    }
}