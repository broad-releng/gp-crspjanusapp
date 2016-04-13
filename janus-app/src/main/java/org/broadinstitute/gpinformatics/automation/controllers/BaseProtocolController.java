package org.broadinstitute.gpinformatics.automation.controllers;

import com.google.common.base.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.control.ReagentForm;
import org.broadinstitute.gpinformatics.automation.messaging.BettaLIMSMessageSender;
import org.broadinstitute.gpinformatics.automation.messaging.HttpMessageTransport;
import org.broadinstitute.gpinformatics.automation.messaging.Message;
import org.broadinstitute.gpinformatics.automation.model.Reagent;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.broadinstitute.gpinformatics.automation.util.WorklistWriter;
import org.broadinstitute.gpinformatics.automation.util.RootConfig;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class that performs common functions between other Worklist controllers such as writing the worklist
 * and sending the message to update LIMs
 */
public abstract class BaseProtocolController implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(BaseProtocolController.class);

    @FXML
    public TableView<WorklistRow> tableView;

    @FXML
    public Button saveWorklistButton;

    @FXML
    public ProtocolTypes protocolType;

    @FXML
    public Button sendMessageButton;

    @FXML
    public ReagentForm reagentForm;

    @Autowired
    public RootConfig rootConfig;

    @Autowired
    public BettaLIMSMessageSender messageSender;

    @Autowired
    public HttpMessageTransport httpMessageTransport;

    @Autowired
    public User user;

    protected WorklistBuilder worklistBuilder;

    public Reagent reagent;

    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        checkNotNull(tableView, "tableView was not injected by FXML");
        checkNotNull(saveWorklistButton, "saveWorklistButton was not injected by FXML");
        checkNotNull(protocolType, "protocolType was not injected by FXML");
        checkNotNull(sendMessageButton, "sendMessageButton was not injected by FXML");

        if (reagentForm != null) {
            reagent = new Reagent("TE");
            reagent.barcodeProperty().bindBidirectional(reagentForm.barcodeProperty());
            reagent.expirationDateProperty().bindBidirectional(reagentForm.expirationDateProperty());
        }

        worklistBuilder = doOnInitialized();
    }

    /**
     * This abstract method is used to enforce implementing controllers to 'call super' and
     * set {@link BaseProtocolController#worklistBuilder} variable
     */
    public abstract WorklistBuilder doOnInitialized();

    /**
     * This method builds the message for the given {@link BaseProtocolController#protocolType}
     *
     * @return - The message for the {@link BaseProtocolController#protocolType}
     */
    public abstract BettaLIMSMessage buildBettalimsMessage() throws DatatypeConfigurationException;

    /**
     * This method is called when the view needs to be reset back to normal such as when
     * a message is successfully sent.
     */
    public abstract void handleReset();

    /**
     * This method resets the tableview and form before calling implementing
     * classes to do the same
     */
    private void reset() {
        if (tableView != null)
            tableView.getItems().removeAll(tableView.getItems());
        if (reagentForm != null)
            reagentForm.reset();
        handleReset();
    }

    /**
     * Called by FXML build button. Generates the worklist for a given controller
     */
    public void build() {
        if (reagentForm != null) {
            Optional<String> reagentErrorsOpt = reagent.hasErrors();
            if (reagentErrorsOpt.isPresent()) {
                Alerts.error(reagentErrorsOpt.get());
                return;
            }
        }

        try {
            List<WorklistRow> normalizations = worklistBuilder.build();
            tableView.setItems(FXCollections.observableArrayList(normalizations));
            if(normalizations.size() > 0) {
                saveWorklistButton.setDisable(false);
                callBuildSuccess();
            } else {
                Alerts.info("Everything is in spec. Nothing needs to happen");
            }
        } catch (Exception ex) {
            gLog.error("BaseProtocolController: build failed", ex);
            Alerts.error(ex.getMessage());
            saveWorklistButton.setDisable(true);
        }
    }

    public void callBuildSuccess() {
            gLog.info("Successfully built worklist");
    }

    /**
     * Saves worklist with name ProtocolType and timestamp
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
            WorklistWriter.write(outputFile, transfers);
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

    /**
     * This method sends a bettalims message and displays result to user
     */
    public void doSendMessage() {
        gLog.info("BaseProtocolController: attempting to send message");
        BettaLIMSMessage bettaLIMSMessage = null;
        try {
            bettaLIMSMessage = buildBettalimsMessage();
            Message message = new Message(bettaLIMSMessage);
            if(messageSender.sendMessage(message)) {
                gLog.info("BaseProtocolController: Message persisted");
                Alerts.success("Message persisted");
                reset();
            } else {
                String errMsg = "Message Failed: ";
                if(httpMessageTransport.getResponse() != null)
                    errMsg += httpMessageTransport.getResponseText();
                Alerts.error(errMsg);
                gLog.error("BaseProtocolController: Message failed to send with response {}", errMsg);
            }
        } catch (DatatypeConfigurationException e) {
            gLog.error("BaseProtocolController: Failed to get date", e);
            Alerts.error("Error building message");
        }
    }

    public List<WorklistRow> getWorklist() {
        return tableView.getItems();
    }
}
