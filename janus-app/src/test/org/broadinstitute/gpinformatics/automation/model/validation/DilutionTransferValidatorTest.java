package org.broadinstitute.gpinformatics.automation.model.validation;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolumeAndConcentration;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.DilutionWorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DilutionTransferValidatorTest {
    private LimsService limsService;
    private Rack source;
    private Rack destintation;
    private DilutionTransferValidator validator;

    @Before
    public void setUp() throws Exception {
        source = new Rack();
        destintation = new Rack();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuild() throws Exception {
        DilutionWorklistBuilder builder =
                new DilutionWorklistBuilder(source, destintation, null, limsService);
        validator = new DilutionTransferValidator(
                source, destintation, builder, limsService);
        source.addTube(0, 0, "srctube1");
        destintation.addTube(0, 0, "desttube1");
        validator.build();
    }

    @Test
    public void testSuccessfulBuild() throws Exception {
        String sourceTubebarcode = "0116404297";
        List<String> sourceTubes = Arrays.asList(sourceTubebarcode);

        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType ConcentrationAndVolumeAndWeightType = new ConcentrationAndVolumeAndWeightType();
        ConcentrationAndVolumeAndWeightType.setConcentration(5);
        ConcentrationAndVolumeAndWeightType.setVolume(20);
        ConcentrationAndVolumeAndWeightType.setTubeBarcode(sourceTubebarcode);
        concentrationAndVolumeAndWeightTypes.put(sourceTubebarcode, ConcentrationAndVolumeAndWeightType);

        limsService = mock(LimsService.class);
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(sourceTubes))
                .thenReturn(concentrationAndVolumeAndWeightTypes);
        when(limsService.doesLimsRecognizeAllTubes(sourceTubes))
                .thenReturn(true);

        TargetVolumeAndConcentration targetVolumeAndConcentration =
                new TargetVolumeAndConcentration();
        targetVolumeAndConcentration.setTargetVolume("10");
        targetVolumeAndConcentration.setTargetConcentration("3");
        DilutionWorklistBuilder builder =
                new DilutionWorklistBuilder(source, destintation, targetVolumeAndConcentration, limsService);
        validator = new DilutionTransferValidator(
                source, destintation, builder, limsService);
        source.addTube(0, 0, sourceTubebarcode);
        source.setBarcode("srcbarcode");
        destintation.addTube(0, 0, "DESTTUBEBARCODE");
        destintation.setBarcode("destbarcode");
        List<WorklistRow> rows = validator.build();
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() == 1);

        DilutionTransfer transfer = (DilutionTransfer) rows.get(0);
        Assert.assertEquals(sourceTubebarcode, transfer.getSourceBarcode());
        Assert.assertEquals("A1", transfer.getSourceWell());
        Assert.assertEquals(14.0, transfer.getNewSourceVolume());
        Assert.assertEquals(6.0, transfer.getDna());
        Assert.assertEquals(4.0, transfer.getTe());
    }
}
