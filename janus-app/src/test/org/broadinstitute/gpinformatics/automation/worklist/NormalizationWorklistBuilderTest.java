package org.broadinstitute.gpinformatics.automation.worklist;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
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

public class NormalizationWorklistBuilderTest {
    private LimsService limsService;
    private Rack source;

    @Before
    public void setUp() throws Exception {
        source = new Rack();
    }

    /**
     * Should norm conc from 10 to 5 by adding TE by formula ((c1 * v1) / c2) - v1
     * or an expected value of ((10 * 20) / 5) - 20 = 20 uL of TE
     * Thus v2 = V1 + TE = 20 + 20 = 40
     * @throws Exception
     */
    @Test
    public void testSuccessfulBuild() throws Exception {
        double V1 = 20;
        double C1 = 10;
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
        NormalizationWorklistBuilder builder =
                new NormalizationWorklistBuilder(source, targetVolume, limsService);
        source.addTube(0, 0, sourceTubebarcode);
        source.setBarcode("srcbarcode");
        List<WorklistRow> rows = builder.build();
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() == 1);

        Normalization transfer = (Normalization) rows.get(0);
        Assert.assertEquals(sourceTubebarcode, transfer.getBarcode());
        Assert.assertEquals("A1", transfer.getWell());
        Assert.assertEquals(40.0, transfer.getV2());
        Assert.assertEquals(20.0, transfer.getTe());
        Assert.assertEquals(5.0, transfer.getC2());
    }
}
