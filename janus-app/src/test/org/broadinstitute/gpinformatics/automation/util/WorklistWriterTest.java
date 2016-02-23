package org.broadinstitute.gpinformatics.automation.util;

import au.com.bytecode.opencsv.CSVReader;
import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.TransferType;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorklistWriterTest {
    private File file;

    @Before
    public void setUp() throws Exception {
        file = new File("janus-app/src/test/MESSAGING/ROBO/test.csv");
        if(file.exists())
            file.delete();
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull() throws IOException {
        WorklistWriter.write(file, null);
    }

    @Test
    public void testWriteNormalization() throws IOException {
        List<WorklistRow> transfer = new ArrayList<>();
        Normalization normalization = new Normalization(true, false);
        normalization.setWell("A1");
        normalization.setTe(3.33);
        transfer.add(normalization);
        WorklistWriter.write(file, transfer);
        Assert.assertTrue(file.exists());
        try(CSVReader reader = new CSVReader(new FileReader(file))){
            String [] nextLine = reader.readNext();
            Assert.assertTrue(nextLine.length == 3);
            String src = nextLine[0];
            String well = nextLine[1];
            String eb = nextLine[2];
            Assert.assertEquals("SRC", src);
            Assert.assertEquals("A1", well);
            Assert.assertEquals("3.33", eb);
        } catch (Exception e) {
            Assert.fail("Should be able to read file");
        }
    }

    @Test(expected = RuntimeException.class)
    public void testWriteDilutionWithNegativeSourceVolume() throws IOException {
        List<WorklistRow> rows = new ArrayList<>();
        DilutionTransfer transfer = new DilutionTransfer();
        transfer.setSourceBarcode("000111222");
        transfer.setDna(50);
        transfer.setTe(50);
        transfer.setNewSourceVolume(-50);
        transfer.setSourceConcentration(10);
        transfer.setTargetVolume(30);
        transfer.setSourceWell("A01");
        transfer.setSourceVolume(0);
        transfer.setTargetConcentration(6);
        transfer.setDestinationWell("A01");
        transfer.setDestinationBarcode("000999888777");
        rows.add(transfer);
        WorklistWriter.write(file, rows);
    }

    @Test
    public void testWriterSortsWorklistOnWell() throws IOException {
        List<WorklistRow> transfers = new ArrayList<>();
        for(char row = 'H'; row >= 'A'; row--) {
            for(int col = 12; col >= 1; col--){
                Normalization normalization = new Normalization(false, false);
                String well = String.format("%c%d", row, col);
                String barcode = "TubeBarcode" + well;
                normalization.setWell(well);
                normalization.setBarcode(barcode);
                transfers.add(normalization);
            }
        }

        List<String> expectedOutputWells = new ArrayList<>();
        for(int col = 1; col <= 12; col++){
            for(char row = 'A'; row <= 'H'; row++) {
                String well = String.format("%c%d", row, col);
                expectedOutputWells.add(well);
            }
        }

        WorklistWriter.write(file, transfers);
        try(CSVReader reader = new CSVReader(new FileReader(file))){
            String [] nextLine;
            int index = 0;
            while((nextLine = reader.readNext()) != null) {
                String expectedWell = expectedOutputWells.get(index);
                String actualWell = nextLine[1];
                Assert.assertEquals(expectedWell, actualWell);
                index++;
            }
        } catch (Exception e) {
            Assert.fail("Should be able to read file");
        }
    }

    @Test
    public void testNormAndSpikeWriting() throws Exception {
        List<WorklistRow> transfers = new ArrayList<>();
        for(char row = 'H'; row >= 'A'; row--) {
            for(int col = 12; col >= 1; col--){
                NormalizationAndSpikeRow normalization = new NormalizationAndSpikeRow(3.5, 6);
                String well = String.format("%c%d", row, col);
                normalization.setParentWell(well);
                normalization.setDaughterWell(well);
                normalization.setDna(2);
                normalization.setTe(3);
                if(col % 2 == 0) {
                    normalization.setTransferType(TransferType.SPIKE);
                } else {
                    normalization.setTransferType(TransferType.NORMALIZATION);
                }
                transfers.add(normalization);
            }
        }

        List<String> expectedOutputWells = new ArrayList<>();
        for(int col = 1; col <= 12; col++){
            for(char row = 'A'; row <= 'H'; row++) {
                String well = String.format("%c%d", row, col);
                expectedOutputWells.add(well);
            }
        }

        WorklistWriter.write(file, transfers);
        try(CSVReader reader = new CSVReader(new FileReader(file))){
            String [] nextLine;
            int index = 0;
            while((nextLine = reader.readNext()) != null) {
                String expectedWell = expectedOutputWells.get(index);
                String actualWell = nextLine[1];
                Assert.assertEquals(expectedWell, actualWell);
                index++;
            }
        } catch (Exception e) {
            Assert.fail("Should be able to read file");
        }
    }
}
