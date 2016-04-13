package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.broadinstitute.gpinformatics.automation.model.validation.DilutionTransferValidator;
import org.broadinstitute.gpinformatics.automation.worklist.AllSampleTransferWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller that handles the Sonic Daughter Plate Creation view
 */
public class SonicDaughterPlateCreationController extends PlateTransferEventController {

    @FXML
    public Button transferButton;

    @Autowired
    private LimsService limsService;

    public SonicDaughterPlateCreationController() {
        super(MessageAttributeTypes.IncludeConcentration.FALSE, MessageAttributeTypes.IncludeVolume.TRUE);
    }

    @Override
    public WorklistBuilder doOnInitialized() {
        assert sourceRackPane != null : "sourceRackPane was not injected by FXML";
        assert destinationRackPane != null : "destinationRackPane was not injected by FXML";
        assert transferButton != null : "transferButton was not injected by FXML";

        //Bind Form
        setSourceRack(sourceRackPane.getRack());
        setDestinationRack(destinationRackPane.getRack());

        AllSampleTransferWorklistBuilder worklistBuilder = new AllSampleTransferWorklistBuilder(
                getSourceRack(), getDestinationRack(), limsService);
        return new DilutionTransferValidator(
                getSourceRack(), getDestinationRack(), worklistBuilder, limsService);
    }

    @Override
    public void handleReset() {
        sourceRackPane.getRack().reset();
        destinationRackPane.getRack().reset();
    }
}
