package org.broadinstitute.gpinformatics.automation.worklist;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.TransferType;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.model.validation.NormalizationAndSpikeTransferValidator;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LibraryDataType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.broadinstitute.techdev.lims.mercury.SampleInfoType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NormAndSpikeWorklistBuilderTest {

    private LimsService limsService;


    @Before
    public void setUp() throws Exception {
        limsService = mock(LimsService.class);
    }

    @Test
    public void testSuccessfulBuild() throws Exception {
        //Parent Tube: 1101214643 for 0171708564
        Rack parentRack = new Rack();
        parentRack.setBarcode("parentrackbarcode");
        parentRack.addTube(0, 0, "0175362322");
        parentRack.addTube(1, 0, "0175362281");

        Rack daughterRack = new Rack();
        daughterRack.setBarcode("daughterrackbarcode");
        daughterRack.addTube(0, 0, "0175359071");
        daughterRack.addTube(1, 0, "0175359048");

        TargetConcentration spikeTargetConc = new TargetConcentration();
        TargetConcentration normTargetConc = new TargetConcentration();
        spikeTargetConc.setTargetConcentration("5");
        normTargetConc.setTargetConcentration("7");

        DoubleProperty spikeMax = new SimpleDoubleProperty(5);
        DoubleProperty normMin = new SimpleDoubleProperty(10);

        double daughterSpikeVol = 10;
        double daughterSpikeConc = 4;
        double daughterNormVol = 10;
        double daughterNormConc = 12;
        Map<String, ConcentrationAndVolumeAndWeightType> daughterMap =
                mockQuantsAndSampleID("0175359071", daughterSpikeConc, daughterSpikeVol); //Needs a spike
        daughterMap.putAll(mockQuantsAndSampleID("0175359048", daughterNormConc, daughterNormVol)); //Needs to be normed

        double parentSpikeConc = 20;
        Map<String, ConcentrationAndVolumeAndWeightType> parentMap =
                mockQuantsAndSampleID("0175362322", 20, 20);
        parentMap.putAll(mockQuantsAndSampleID("0175362281", 30, 20));

        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(anyList())).thenReturn(
                daughterMap, //Going to get called once by validator
                daughterMap, //Called again during build
                parentMap
        );

        when(limsService.fetchLibraryDetailsByTubeBarcode(anyList(), anyBoolean())).thenReturn(
                buildLibraryDetailsForTubeBarcode("SM-1"),
                buildLibraryDetailsForTubeBarcode("SM-2"),
                buildLibraryDetailsForTubeBarcode("SM-1"),
                buildLibraryDetailsForTubeBarcode("SM-2")
        );

        NormAndSpikeWorklistBuilder builder = new NormAndSpikeWorklistBuilder(parentRack, daughterRack,
                spikeTargetConc, normTargetConc, spikeMax, normMin, limsService);
        NormalizationAndSpikeTransferValidator validator = new NormalizationAndSpikeTransferValidator(
                parentRack, daughterRack, spikeMax, builder, limsService
        );
        List<WorklistRow> rows = validator.build();
        Assert.assertEquals(2, rows.size());

        NormalizationAndSpikeRow expectedSpikeRow = (NormalizationAndSpikeRow) rows.get(0);
        NormalizationAndSpikeRow expectedNormRow = (NormalizationAndSpikeRow) rows.get(1);

        //Spikes Sample: (Target * Vd ) - (Vd * Cd))/ (Cs - Target)
        Assert.assertEquals(TransferType.SPIKE, expectedSpikeRow.getTransferType());
        Assert.assertEquals("0175359071", expectedSpikeRow.getDaughterBarcode());
        double dnaToSpike = ((spikeTargetConc.getTargetConcentration() * daughterSpikeVol) -
                            (daughterSpikeConc * daughterSpikeVol)) /
                            (parentSpikeConc - spikeTargetConc.getTargetConcentration());
        Assert.assertEquals(dnaToSpike, expectedSpikeRow.getDna(), 0.1);

        //Norms TE: ((Vd * Cd) / Target) - Vd
        Assert.assertEquals(TransferType.NORMALIZATION, expectedNormRow.getTransferType());
        double expectedTeToAdd =
                ((daughterNormVol * daughterNormConc) / normTargetConc.getTargetConcentration()) - daughterNormVol;

        Assert.assertEquals(expectedTeToAdd, expectedNormRow.getTe(), 0.1);
    }

    @Test
    public void testSuccessfulBuildOfNormsOnlyWithValidator() throws Exception {
        Rack daughterRack = new Rack();
        daughterRack.setBarcode("daughterrackbarcode");
        daughterRack.addTube(0, 0, "0175359071");
        daughterRack.addTube(1, 0, "0175359048");

        TargetConcentration spikeTargetConc = new TargetConcentration();
        TargetConcentration normTargetConc = new TargetConcentration();
        spikeTargetConc.setTargetConcentration("5");
        normTargetConc.setTargetConcentration("7");

        DoubleProperty spikeMax = new SimpleDoubleProperty(5);
        DoubleProperty normMin = new SimpleDoubleProperty(10);

        double daughterNormVol = 10;
        double daughterNormConc = 12;
        Map<String, ConcentrationAndVolumeAndWeightType> daughterMap =
                mockQuantsAndSampleID("0175359048", daughterNormConc, daughterNormVol);

        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(anyList())).thenReturn(
                daughterMap
        );

        when(limsService.fetchLibraryDetailsByTubeBarcode(anyList(), anyBoolean())).thenReturn(
                buildLibraryDetailsForTubeBarcode("SM-1")
        );

        NormAndSpikeWorklistBuilder builder = new NormAndSpikeWorklistBuilder(null, daughterRack,
                spikeTargetConc, normTargetConc, spikeMax, normMin, limsService);

        NormalizationAndSpikeTransferValidator validator = new NormalizationAndSpikeTransferValidator(
                null, daughterRack, spikeMax, builder, limsService
        );

        List<WorklistRow> rows = validator.build();
        Assert.assertEquals(1, rows.size());

        NormalizationAndSpikeRow expectedNormRow = (NormalizationAndSpikeRow) rows.get(0);

        //Norms TE: ((Vd * Cd) / Target) - Vd
        Assert.assertEquals(TransferType.NORMALIZATION, expectedNormRow.getTransferType());
        double expectedTeToAdd =
                ((daughterNormVol * daughterNormConc) / normTargetConc.getTargetConcentration()) - daughterNormVol;

        Assert.assertEquals(expectedTeToAdd, expectedNormRow.getTe(), 0.1);
    }

    private List<LibraryDataType> buildLibraryDetailsForTubeBarcode(String sampleName) {
        List<LibraryDataType> libraryDataTypes = new ArrayList<>();
        LibraryDataType libraryDataType = new LibraryDataType();
        libraryDataType.setWasFound(true);
        libraryDataTypes.add(libraryDataType);
        SampleInfoType sampleInfoType = new SampleInfoType();
        sampleInfoType.setSampleName(sampleName);
        libraryDataType.getSampleDetails().add(sampleInfoType);
        return libraryDataTypes;
    }

    public Map<String, ConcentrationAndVolumeAndWeightType> mockQuantsAndSampleID(String tubeBarcode,
                                                                                  double conc, double vol) {
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypeMap =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType concentrationAndVolumeAndWeightType =
                new ConcentrationAndVolumeAndWeightType();
        concentrationAndVolumeAndWeightType.setTubeBarcode(tubeBarcode);
        concentrationAndVolumeAndWeightType.setConcentration(conc);
        concentrationAndVolumeAndWeightType.setVolume(vol);
        concentrationAndVolumeAndWeightTypeMap.put(tubeBarcode, concentrationAndVolumeAndWeightType);
        return concentrationAndVolumeAndWeightTypeMap;
    }
}