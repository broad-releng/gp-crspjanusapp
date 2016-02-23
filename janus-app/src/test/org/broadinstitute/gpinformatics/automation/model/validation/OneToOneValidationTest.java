package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.junit.Assert;
import org.junit.Test;

public class OneToOneValidationTest {
    @Test
    public void testIsValidChecksNull() throws Exception {
        OneToOneValidation validation = new OneToOneValidation(null, null);
        boolean isValid = validation.isValid();
        Assert.assertFalse(isValid);
    }

    @Test
    public void testIsValidForOneToOne() throws Exception {
        Rack source = new Rack();
        Rack destination = new Rack();
        source.addTube(0, 0, "srctube");
        destination.addTube(0, 0, "desttube");
        OneToOneValidation validation = new OneToOneValidation(source, destination);
        boolean isValid = validation.isValid();
        Assert.assertTrue(isValid);

        destination.addTube(0, 1, "desttube2");
        Assert.assertFalse(validation.isValid());
    }
}
