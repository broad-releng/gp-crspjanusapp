package org.broadinstitute.gpinformatics.automation.worklist;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.model.validation.SourceRackOnlyValidator;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RaiseVolumeWorklistBuilderTest {
    private LimsService limsService;
    private Rack source;

    @Before
    public void setUp() throws Exception {
        source = new Rack();
    }

    /**
     * Should raise volume fro 20 to 30 with TE amount of 10
     * Should cause concentration to be diluted by formula (v1 + c1) / v2
     * or (20 + 10) / 30 = 1
     * @throws Exception
     */
    @Test
    public void testSuccessfulBuild() throws Exception {
        String sourceTubebarcode = "0116404297";
        List<String> sourceTubes = Arrays.asList(sourceTubebarcode);

        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType concentrationAndVolumeAndWeightType = new ConcentrationAndVolumeAndWeightType();
        concentrationAndVolumeAndWeightType.setVolume(20);
        concentrationAndVolumeAndWeightType.setConcentration(10);
        concentrationAndVolumeAndWeightType.setTubeBarcode(sourceTubebarcode);
        concentrationAndVolumeAndWeightTypes.put(sourceTubebarcode, concentrationAndVolumeAndWeightType);

        limsService = mock(LimsService.class);
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(sourceTubes))
                .thenReturn(concentrationAndVolumeAndWeightTypes);
        when(limsService.doesLimsRecognizeAllTubes(sourceTubes))
                .thenReturn(true);

        TargetVolume targetVolume =
                new TargetVolume();
        targetVolume.setTargetVolume("30");
        RaiseVolumeWorklistBuilder builder =
                new RaiseVolumeWorklistBuilder(source, limsService, targetVolume);
        source.addTube(0, 0, sourceTubebarcode);
        source.setBarcode("srcbarcode");
        List<WorklistRow> rows = builder.build();
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() == 1);

        Normalization transfer = (Normalization) rows.get(0);
        Assert.assertEquals(sourceTubebarcode, transfer.getBarcode());
        Assert.assertEquals("A1", transfer.getWell());
        Assert.assertEquals(30.0, transfer.getV2());
        Assert.assertEquals(10.0, transfer.getTe());
        Assert.assertEquals(6.66, transfer.getC2(), .1f);
    }
}
