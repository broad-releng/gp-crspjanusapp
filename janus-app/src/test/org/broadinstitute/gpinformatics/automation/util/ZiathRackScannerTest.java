package org.broadinstitute.gpinformatics.automation.util;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZiathRackScannerTest {

    @Test
    public void testFire() throws Exception {
        File outputDir = new File("janus-app/src/test/Ziath");
        RuntimeExecutor executor = mock(RuntimeExecutor.class);
        when(executor.execute("fakecmd")).thenReturn("");

        //File won't be generated so return true to avoid exception
        RackScanner rackScanner = new ZiathRackScanner(executor, outputDir) {
            @Override
            protected void deleteOutputFileIfExists() {
                //Do nothing
            }
        };
        Rack rack = rackScanner.fire();
        List<Tube> tubes = rack.getTubes();
        Assert.assertTrue(tubes.size() == 3);
        String rackBarcode = rack.getBarcode();
        Assert.assertEquals("000111222333", rackBarcode);
        Tube a1 = tubes.get(0);
        Assert.assertEquals("A1", a1.getWell());
        Assert.assertEquals("0154784860", a1.getBarcode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoOuputFileThrowsException() throws IOException, TimeoutException, TubeDecodeException {
        RuntimeExecutor executor = mock(RuntimeExecutor.class);
        when(executor.execute("fakecmd")).thenReturn("");

        //File won't be generated so return true to avoid exception
        RackScanner rackScanner = new ZiathRackScanner(executor, new File("WrongDir"));
        rackScanner.fire();
    }
}
