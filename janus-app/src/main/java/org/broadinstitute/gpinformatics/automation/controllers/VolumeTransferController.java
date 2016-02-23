package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.gpinformatics.automation.model.validation.VolumeTransferValidator;
import org.broadinstitute.gpinformatics.automation.worklist.VolumeTransferWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller that handles the VolumeTransfer.fxml view
 */
public class VolumeTransferController extends PlateTransferEventController {
    @FXML
    public TextField targetConcentrationTextField;

    @FXML
    public TextField targetVolumeTextField;

    @FXML
    public Button transferButton;

    @Autowired
    private LimsService limsService;

    private TargetVolume targetVolume;

    @Override
    public WorklistBuilder doOnInitialized() {
        assert sourceRackPane != null : "sourceRackPane was not injected by FXML";
        assert destinationRackPane != null : "destinationRackPane was not injected by FXML";
        assert targetConcentrationTextField != null : "targetConcentrationTextField was not injected by FXML";
        assert targetVolumeTextField != null : "targetVolumeTextField was not injected by FXML";
        assert transferButton != null : "transferButton was not injected by FXML";

        //Bind Form
        targetVolume = new TargetVolume();
        targetVolumeTextField.textProperty()
                .bindBidirectional(targetVolume.targetVolumeProperty());
        transferButton.disableProperty().bind(
                targetVolume.isValid());

        setSourceRack(sourceRackPane.getRack());
        setDestinationRack(destinationRackPane.getRack());

        VolumeTransferWorklistBuilder worklistBuilder = new VolumeTransferWorklistBuilder(
                getSourceRack(), getDestinationRack(), limsService, targetVolume);
        return new VolumeTransferValidator(
                getSourceRack(), getDestinationRack(), worklistBuilder, limsService, targetVolume);
    }

    @Override
    public void handleReset() {
        sourceRackPane.getRack().reset();
        destinationRackPane.getRack().reset();
        targetVolume.reset();
    }
}
