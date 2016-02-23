package org.broadinstitute.gpinformatics.automation.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizationTest {
    @Test
    public void testVolumeAdditionChangeTargetsAdjustsFields() {
        Normalization normalization = new Normalization(false, false);
        normalization.setWell("A1");
        normalization.setBarcode("srctube01");
        normalization.setV1(20);
        normalization.setC1(20);
        normalization.setTarget(40);
        double c2 = normalization.getC2();
        double v2 = normalization.getV2();
        double te = normalization.getTe();
        assertEquals(10, c2, 0.0f);
        assertEquals(40, v2, 0.0f);
        assertEquals(20, te, 0.0f);
    }
}