package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.validation.SourceRackOnlyValidator;
import org.broadinstitute.gpinformatics.automation.worklist.NormalizationWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller that handles the Normalization view
 */
public class NormalizationController extends InPlateLabEventController {
    private static final Logger gLog = LoggerFactory.getLogger(NormalizationController.class);

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
}
