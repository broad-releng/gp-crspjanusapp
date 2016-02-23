package org.broadinstitute.gpinformatics.automation.util;

import au.com.bytecode.opencsv.CSVWriter;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Write worklists to given file
 */
public class WorklistWriter {
    private static final Logger gLog = LoggerFactory.getLogger(WorklistWriter.class);

    public static boolean write(File outputFile, List<WorklistRow> transfers) throws IOException {
        validateWorklist(transfers);
        Collections.sort(transfers, new Comparator<WorklistRow>() {
            @Override
            public int compare(WorklistRow o1, WorklistRow o2) {
                String well = o1.sortWell();
                String compareWell = o2.sortWell();

                Integer column = Integer.parseInt(well.substring(1));
                Integer compareColumn = Integer.parseInt(compareWell.substring(1));
                Character row = well.charAt(0);
                Character compareRow = compareWell.charAt(0);

                //A1,B1,C1...A2,B2,C2...
                if(column < compareColumn){
                    return -1;
                }else if(column.equals(compareColumn)){
                    return row.compareTo(compareRow);
                }else{
                    return 1;
                }
            }
        });
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile), ',', CSVWriter.NO_QUOTE_CHARACTER)){
            for(WorklistRow transfer: transfers) {
                String[] row = transfer.toWorklistRow();
                writer.writeNext(row);
            }
            return true;
        } catch (IOException e) {
            gLog.error("WorklistWriter: failed to write file", e);
            throw e;
        }
    }

    private static void validateWorklist(List<WorklistRow> transfers) {
        String genericErrMsg = "%s will result in a negative number. Please fix before continuing";
        for(WorklistRow row : transfers) {
            if(row instanceof DilutionTransfer) {
                DilutionTransfer transfer = (DilutionTransfer) row;
                if(transfer.getNewSourceVolume() < 0) {
                    throw new RuntimeException(
                            String.format(genericErrMsg, "New Source Volume"));
                }
            } else if (row instanceof Normalization) {
                Normalization normalization = (Normalization) row;
                if(normalization.getV2() < 0) {
                    throw new RuntimeException(
                            String.format(genericErrMsg, "V2")
                    );
                }
                if(normalization.getC2() < 0) {
                    throw new RuntimeException(
                            String.format(genericErrMsg, "C2")
                    );
                }
            }
        }
    }
}
