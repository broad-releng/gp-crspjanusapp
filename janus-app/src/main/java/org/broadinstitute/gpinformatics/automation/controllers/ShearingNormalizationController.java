package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.model.TargetVolumeAndConcentration;
import org.broadinstitute.gpinformatics.automation.model.validation.DilutionTransferValidator;
import org.broadinstitute.gpinformatics.automation.worklist.DilutionWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller that handles the Shearing Transfer view
 */
public class ShearingNormalizationController extends PlateTransferEventController {

    @FXML
    public TextField targetConcentrationTextField;

    @FXML
    public TextField targetVolumeTextField;

    @FXML
    public Button transferButton;

    @Autowired
    private LimsService limsService;

    private TargetVolumeAndConcentration targetVolumeAndConcentration;

    @Override
    public WorklistBuilder doOnInitialized() {
        assert sourceRackPane != null : "sourceRackPane was not injected by FXML";
        assert destinationRackPane != null : "destinationRackPane was not injected by FXML";
        assert targetConcentrationTextField != null : "targetConcentrationTextField was not injected by FXML";
        assert targetVolumeTextField != null : "targetVolumeTextField was not injected by FXML";
        assert transferButton != null : "transferButton was not injected by FXML";

        //Bind Form
        targetVolumeAndConcentration = new TargetVolumeAndConcentration();
        targetConcentrationTextField.textProperty()
                .bindBidirectional(targetVolumeAndConcentration.targetConcentrationProperty());
        targetVolumeTextField.textProperty()
                .bindBidirectional(targetVolumeAndConcentration.targetVolumeProperty());
        transferButton.disableProperty().bind(
                targetVolumeAndConcentration.isValid());

        setSourceRack(sourceRackPane.getRack());
        setDestinationRack(destinationRackPane.getRack());

        DilutionWorklistBuilder dilutionWorklistBuilder = new DilutionWorklistBuilder(
                getSourceRack(), getDestinationRack(), targetVolumeAndConcentration, limsService);
        return new DilutionTransferValidator(
                getSourceRack(), getDestinationRack(), dilutionWorklistBuilder, limsService);
    }

    @Override
    public void handleReset() {
        sourceRackPane.getRack().reset();
        destinationRackPane.getRack().reset();
        targetVolumeAndConcentration.reset();
    }
}
