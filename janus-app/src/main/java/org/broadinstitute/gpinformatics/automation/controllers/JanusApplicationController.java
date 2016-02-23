package org.broadinstitute.gpinformatics.automation.controllers;

import com.sun.javafx.util.MessageBus;
import com.sun.javafx.util.MessageBusEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.event.UserLoginEvent;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.util.RootConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for CrspJanusApplication.fxml file. Handles user login/logout
 */
public class JanusApplicationController extends StackPaneController implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(JanusApplicationController.class);

    @FXML
    public Label loggedInUserLabel;

    @FXML
    public StackPane stackPane;

    @FXML
    public Pane loginPane;

    @FXML
    public Pane janusProtocolsPane;

    @FXML
    public Button logoutButton;

    @FXML
    public Label appVersionLabel;

    @FXML
    public Label machineNameLabel;

    @FXML
    public Button closeButton;

    @Autowired
    public User user;

    @Autowired
    public MachineNameLookup machineNameLookup;

    @Autowired
    public String appVersion;

    @Autowired
    public RootConfig rootConfig;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        replaceContent(loginPane);
        initializeLoginListener();
        checkMachineName();
        logoutButton.visibleProperty().bind(
                Bindings.when(
                        loggedInUserLabel.textProperty().isNotEqualTo("")
                ).then(true).otherwise(false)
        );
        appVersionLabel.setText(appVersion);
        try {
            rootConfig.moveRoboDirectoryContentsToArchive();
        } catch (IOException e) {
            String errMsg = "Could not archive old worklists";
            gLog.error("JanusApplicationController: {}", errMsg);
            Alerts.error(errMsg);
        }
    }

    /**
     * Event handler for logout button pressed
     */
    public void logout() {
        loggedInUserLabel.setText("");
        user.setUsername("");
        replaceContent(loginPane);
    }

    /**
     * Event handler to close application
     */
    public void closeApplication() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Listen for UserLoginEvents to switch view back to protocols
     */
    private void initializeLoginListener() {
        MessageBus.subscribe(UserLoginEvent.class, new EventHandler<MessageBusEvent<UserLoginEvent>>() {
            @Override
            public void handle(MessageBusEvent<UserLoginEvent> userLoginEventMessageBusEvent) {
                gLog.info("ProtocolsController: user login event triggered {}", userLoginEventMessageBusEvent);
                UserLoginEvent loginEvent = userLoginEventMessageBusEvent.getMessage();
                loggedInUserLabel.setText("Logged in as " + loginEvent.getUser().getUsername());
                replaceContent(janusProtocolsPane);
            }
        });
    }

    private void checkMachineName() {
        try {
            String machineName = machineNameLookup.getMachineName();
            machineNameLabel.setText(machineName);
        } catch (IOException e) {
            gLog.error("JanusApplicationController: error grabbing machine name", e);
            Alerts.error("Machine name needs to set in super_init.json file. Aborting...");
            throw new RuntimeException("Could not fetch machine name");
        }
    }
}
