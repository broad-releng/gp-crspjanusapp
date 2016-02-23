package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.gpinformatics.automation.model.validation.SourceRackOnlyValidator;
import org.broadinstitute.gpinformatics.automation.worklist.RaiseVolumeWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.util.RootConfig;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller that handles the Raise Volume view
 */
public class RaiseVolumeController extends InPlateLabEventController {
    private static final Logger gLog = LoggerFactory.getLogger(RaiseVolumeController.class);

    @FXML
    public Button buildButton;

    @FXML
    public TextField targetVolumeTextField;

    @Autowired
    public LimsService limsService;

    @Autowired
    public RootConfig rootConfig;

    private TargetVolume targetVolume;

    public RaiseVolumeController() {
        super(MessageAttributeTypes.IncludeConcentration.FALSE, MessageAttributeTypes.IncludeVolume.TRUE);
    }

    @Override
    public WorklistBuilder doOnInitialized() {
        targetVolume = new TargetVolume();
        targetVolumeTextField.textProperty().bindBidirectional(targetVolume.targetVolumeProperty());
        buildButton.disableProperty().bind(
                targetVolume.isValid());
        Rack rack = rackPane.getRack();
        setRack(rack);

        RaiseVolumeWorklistBuilder builder =
                new RaiseVolumeWorklistBuilder(getRack(), limsService, targetVolume);
        return new SourceRackOnlyValidator(
                builder, getRack(), limsService);
    }

    public void build() {
        gLog.info("RaiseVolumeController: build() {} {}", targetVolume, rackPane.getRack());
        super.build();
    }

    @Override
    public void handleReset() {
        rackPane.getRack().reset();
        targetVolume.reset();
    }
}
