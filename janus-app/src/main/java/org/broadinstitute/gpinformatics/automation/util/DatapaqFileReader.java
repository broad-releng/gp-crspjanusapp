package org.broadinstitute.gpinformatics.automation.util;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse Datapaq output from server.exe call
 *
 * example:
 * Data,RackBarcode,Row,Col,tubeBarcode
 * Aug 26, 2014 12:12:12 PM,000111222333,A,1,0154784860
 */
public class DatapaqFileReader {

    /**
     * Reads generated file from a datapaq rack scan, returning the parsed results.
     *
     * @param file The file to parse
     * @return List of the parsed results
     * @throws IOException if file does not exist
     * @throws TubeDecodeException if tube decode failure found
     */
    public List<DatapaqRackScanResult> read(File file) throws IOException, TubeDecodeException {
        try (CSVReader reader = new CSVReader(new FileReader(file), ',', '\'', 1)) {
            List<DatapaqRackScanResult> tubes = new ArrayList<>();
            String[] result;
            while((result = reader.readNext()) != null) {
                String rackBarcode = result[2];
                String row = result[3];
                int col = Integer.parseInt(result[4]);
                String tubeBarcode = result[5];

                if(tubeBarcode.equalsIgnoreCase("DECODE FAILURE")) {
                    String err = String.format("Tube decode fail at %s%d", row, col);
                    throw new TubeDecodeException(err);
                } else if(!tubeBarcode.equalsIgnoreCase("EMPTY")) {
                    DatapaqRackScanResult datapaqRackScanResult =
                            new DatapaqRackScanResult();
                    datapaqRackScanResult.setColumn(col);
                    datapaqRackScanResult.setRow(row);
                    datapaqRackScanResult.setTubeBarcode(tubeBarcode);
                    datapaqRackScanResult.setRackBarcode(rackBarcode);
                    tubes.add(datapaqRackScanResult);
                }
            }

            return tubes;
        }
    }
}
