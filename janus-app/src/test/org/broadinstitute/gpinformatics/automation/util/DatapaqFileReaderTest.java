package org.broadinstitute.gpinformatics.automation.util;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DatapaqFileReaderTest {
    File dir = new File("src/test/Ziath");
    File file = new File(dir, "ExampleScan.txt");
    File decodeFail = new File(dir, "DecodeFailure.txt");

    @Test
    public void testRead() throws Exception {
        DatapaqFileReader fileReader = new DatapaqFileReader();
        List<DatapaqRackScanResult> results = fileReader.read(file);
        Assert.assertTrue(results.size() == 3);

        DatapaqRackScanResult a1 = results.get(0);
        Assert.assertEquals("A", a1.getRow());
        Assert.assertEquals(1, a1.getColumn());
    }

    @Test(expected = TubeDecodeException.class)
    public void testDecodeFailureRead() throws IOException, TubeDecodeException {
        DatapaqFileReader fileReader = new DatapaqFileReader();
        fileReader.read(decodeFail);
    }
}
