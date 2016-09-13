package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.model.validation.SourceRackOnlyValidator;
import org.broadinstitute.gpinformatics.automation.util.WorklistWriter;
import org.broadinstitute.gpinformatics.automation.worklist.NormalizationWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller that handles the Normalization view
 */
public class NormalizationController extends InPlateLabEventController {
    private static final Logger gLog = LoggerFactory.getLogger(NormalizationController.class);
    public static final double MAX_TIP_SIZE = 200;

    @FXML
    public TextField targetConcentrationTextField;

    @FXML
    public Button buildButton;

    @Autowired
    private LimsService limsService;

    private TargetConcentration targetConcentration;

    public NormalizationController() {
        super(MessageAttributeTypes.IncludeConcentration.TRUE, MessageAttributeTypes.IncludeVolume.TRUE);
    }

    @Override
    public WorklistBuilder doOnInitialized() {
        targetConcentration = new TargetConcentration();
        targetConcentrationTextField.textProperty().bindBidirectional(
                targetConcentration.targetConcentrationProperty());
        buildButton.disableProperty().bind(targetConcentration.isValid());
        setRack(rackPane.getRack());
        NormalizationWorklistBuilder builder = new NormalizationWorklistBuilder(
                getRack(), targetConcentration, limsService);
        return new SourceRackOnlyValidator(builder, getRack(), limsService);
    }

    public void build() {
        gLog.info("NormalizationController: build() {} {}", targetConcentration, rackPane.getRack());
        super.build();
    }

    @Override
    public void handleReset() {
        rackPane.getRack().reset();
        targetConcentration.reset();
    }

    public List<WorklistRow> splitTransfers(List<WorklistRow> transfers) {
        List<WorklistRow> allTransfers = new ArrayList<>();
        for (WorklistRow row: transfers) {
            Normalization norm = (Normalization) row;
            allTransfers.add(norm);
            if (norm.getTe() > MAX_TIP_SIZE) {
                double remainingTe = norm.getTe();
                norm.setTe(MAX_TIP_SIZE);
                remainingTe -= MAX_TIP_SIZE;
                while (true) {
                    Normalization newNorm = new Normalization(true, norm.isInSpecification());
                    newNorm.setWell(norm.getWell());
                    allTransfers.add(newNorm);
                    if (remainingTe < MAX_TIP_SIZE) {
                        newNorm.setTe(remainingTe);
                        break;
                    } else {
                        remainingTe -= MAX_TIP_SIZE;
                        newNorm.setTe(MAX_TIP_SIZE);
                    }
                }
            }
        }
        return allTransfers;
    }

    @Override
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
            //Build new list of transfers to split overflows
            List<WorklistRow> allTransfers = splitTransfers(transfers);
            WorklistWriter.write(outputFile, allTransfers);
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
