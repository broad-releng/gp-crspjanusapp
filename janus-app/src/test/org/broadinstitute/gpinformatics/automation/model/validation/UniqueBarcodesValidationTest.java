package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.junit.Assert;
import org.junit.Test;

public class UniqueBarcodesValidationTest {

    @Test
    public void testIsValidChecksNullRacks() throws Exception {
        UniqueBarcodesValidation validation =
                new UniqueBarcodesValidation(null, null);
        boolean isValid = validation.isValid();
        Assert.assertFalse(isValid);
    }

    @Test
    public void testIsValidForOneToOne() throws Exception {
        Rack source = new Rack();
        Rack destination = new Rack();

        source.setBarcode("srcbarcode");
        destination.setBarcode("destbarcode");

        UniqueBarcodesValidation validation =
                new UniqueBarcodesValidation(source, destination);

        boolean isValid = validation.isValid();
        Assert.assertTrue(isValid);

        destination.setBarcode("srcbarcode");
        Assert.assertFalse(validation.isValid());
    }
}
