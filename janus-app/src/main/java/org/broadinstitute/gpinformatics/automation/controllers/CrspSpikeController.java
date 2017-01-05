package org.broadinstitute.gpinformatics.automation.controllers;


import au.com.bytecode.opencsv.CSVWriter;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.util.WorklistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Controller that handles the TruSeq Spike view
 */
public class CrspSpikeController extends NormAndSpikesBaseController {

    private static final Logger gLog = LoggerFactory.getLogger(CrspSpikeController.class);

    /**
     * Needs to save worklist in dilution format
     */
    public void doSaveWorklist() {
        try {
            rootConfig.moveRoboDirectoryContentsToArchive();
        } catch (IOException e) {
            Alerts.error("Could not archive old worklists");
            gLog.error("BaseProtocolController: could not archive old worklists", e);
        }

        List<WorklistRow> transfers = tableView.getItems();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String identifier = sdf.format(new Date());
        String fileName = String.format("%s-%s-%s.csv", protocolType, user.getUsername(), identifier);
        File outputFile = new File(rootConfig.getRoboDirectory(), fileName);
        try {
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
                    String[] row = ((NormalizationAndSpikeRow)transfer).toDilutionWorklistRow();
                    writer.writeNext(row);
                }
            } catch (IOException e) {
                gLog.error("WorklistWriter: failed to write file", e);
                throw e;
            }
            Alerts.info("Worklist created! saved to " + outputFile.getPath());
            sendMessageButton.setDisable(false);
        } catch (IOException e) {
            gLog.error("BaseProtocolController: doSaveWorklist failed writing file", e);
            Alerts.error("Could not write worklist to " + outputFile.getPath());
            sendMessageButton.setDisable(true);
        } catch (Exception e) {
            gLog.error("BaseProtocolController: doSaveWorklist failed to validate", e);
            Alerts.error("Problem generating worklist:" + e.getMessage());
            sendMessageButton.setDisable(true);
        }
    }
}
