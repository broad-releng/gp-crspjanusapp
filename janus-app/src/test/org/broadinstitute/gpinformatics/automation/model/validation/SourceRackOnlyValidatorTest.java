package org.broadinstitute.gpinformatics.automation.model.validation;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.RaiseVolumeWorklistBuilder;
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

public class SourceRackOnlyValidatorTest extends LimsValidator {
    private Rack source;
    private SourceRackOnlyValidator validator;

    @Before
    public void setUp() throws Exception {
        source = new Rack();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFail() throws Exception {
        String sourceTubebarcode = "UNKNOWNTUBE";

        TargetVolume targetVolume = new TargetVolume();
        targetVolume.setTargetVolume("60");
        RaiseVolumeWorklistBuilder builder =
                new RaiseVolumeWorklistBuilder(source, limsService, targetVolume);
        validator = new SourceRackOnlyValidator(
                builder, source, limsService);

        source.addTube(0, 0, sourceTubebarcode);
        source.setBarcode("srcbarcode");

        validator.build();
    }

    @Test
    public void testSuccessfulBuild() throws Exception {
        //First test raise volumes
        String sourceTubebarcode = "0116404297";
        List<String> sourceTubes = Arrays.asList(sourceTubebarcode);

        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightType =
                buildCandV(sourceTubebarcode);

        LimsService limsService = mock(LimsService.class);
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(sourceTubes))
                .thenReturn(concentrationAndVolumeAndWeightType);
        when(limsService.doesLimsRecognizeAllTubes(sourceTubes))
                .thenReturn(true);

        TargetVolume targetVolume = new TargetVolume();
        targetVolume.setTargetVolume("60");
        RaiseVolumeWorklistBuilder builder =
                new RaiseVolumeWorklistBuilder(source, limsService, targetVolume);
        validator = new SourceRackOnlyValidator(
                builder, source, limsService);

        source.addTube(0, 0, sourceTubebarcode);
        source.setBarcode("srcbarcode");

        List<WorklistRow> rows = validator.build();
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() == 1);

        //Should add 40 to volume
        Normalization transfer = (Normalization) rows.get(0);
        Assert.assertEquals(sourceTubebarcode, transfer.getBarcode());
        Assert.assertEquals("A1", transfer.getWell());
        Assert.assertEquals(60.0, transfer.getV2());
        Assert.assertEquals(40.0, transfer.getTe());
    }


    private Map<String, ConcentrationAndVolumeAndWeightType> buildCandV(String barcode){
        Map<String, ConcentrationAndVolumeAndWeightType> ConcentrationAndVolumeAndWeightTypes =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType concentrationAndVolumeAndWeightType = new ConcentrationAndVolumeAndWeightType();
        concentrationAndVolumeAndWeightType.setConcentration(5);
        concentrationAndVolumeAndWeightType.setVolume(20);
        concentrationAndVolumeAndWeightType.setTubeBarcode(barcode);
        ConcentrationAndVolumeAndWeightTypes.put(barcode, concentrationAndVolumeAndWeightType);
        return ConcentrationAndVolumeAndWeightTypes;
    }
}
