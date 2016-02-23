package org.broadinstitute.gpinformatics.automation.model.validation;


import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VolumeCheckTest {
    @Test
    public void testVolumeCheck() throws Exception {
        LimsService limsService = mock(LimsService.class);
        Map<String, ConcentrationAndVolumeAndWeightType> map =
                new HashMap<>();
        ConcentrationAndVolumeAndWeightType con = new ConcentrationAndVolumeAndWeightType();
        con.setVolume(45);
        map.put("barcode", con);
        when(limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(anyList())).thenReturn(map);

        Rack sourceRack = new Rack();
        sourceRack.setBarcode("srcrackbarcode");
        sourceRack.addTube(0, 0, "tubebarcode");

        //How much to transfer
        TargetVolume targetVolume = new TargetVolume();
        targetVolume.setTargetVolume("60");

        VolumeCheck volumeCheck = new VolumeCheck(sourceRack, limsService, targetVolume);
        Assert.assertFalse(volumeCheck.isValid());

        targetVolume.setTargetVolume("30");
        Assert.assertTrue(volumeCheck.isValid());
    }
}
