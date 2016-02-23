package org.broadinstitute.gpinformatics.automation.controllers;

import com.google.common.base.Optional;
import com.sun.javafx.util.MessageBus;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.event.UserLoginEvent;
import org.broadinstitute.gpinformatics.automation.util.BadgeScanner;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * Controller class for Login.fxml view
 */
public class LoginController implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(LoginController.class);
    public static final String BAD_SCAN_MSG = "Could not grab username from badge scan, please type in manually";

    @FXML
    public TextField username;

    @FXML
    public Button signInButton;

    @FXML
    public ProgressIndicator scanningBadgeIndicator;

    @FXML
    public Button badgeScanButton;

    @FXML
    public Label errorMessage;

    @Autowired
    public LimsService limsService;

    @Autowired
    public User user;

    @Autowired
    public BadgeScanner badgeScanner;

    @Autowired
    public File badgeScannerFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        username.textProperty().bindBidirectional(
                user.usernameProperty()
        );

        BooleanBinding disableBinding = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return user.usernameProperty().get().isEmpty();
            }
        }, user.usernameProperty());

        signInButton.disableProperty().bind(disableBinding);
        username.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    if (!user.getUsername().isEmpty()) {
                        handleSubmitButtonAction();
                    }
                }
            }
        });

        fireBadgeScanner();
    }

    /**
     * This method validates and logs in user when submit button is pressed
     * Will send message to ProtocolsController to handle success
     */
    public void handleSubmitButtonAction() {
        gLog.info("LoginController: validating user {}", user);
        MessageBus.sendMessage(new UserLoginEvent(user));
    }

    /**
     * This method attempts to retrieve username from Badge Scan exectuable
     * that gets spawned in a new thread
     */
    public void fireBadgeScanner() {
        if(!badgeScannerFile.exists()) {
            String path = badgeScannerFile.getPath();
            errorMessage.setText(
                    "Badge Scanner executable does not exist. Make sure that path is mapped to " + path);
            return;
        }

        final Task<Optional<String>> task = new Task<Optional<String>>() {

            @Override
            protected Optional<String> call() throws Exception {
                return badgeScanner.getUsernameFromBadgeScan();
            }
        };

        badgeScanButton.disableProperty().bind(task.runningProperty());
        scanningBadgeIndicator.visibleProperty().bind(task.runningProperty());
        username.disableProperty().bind(task.runningProperty());
        signInButton.disableProperty().bind(task.runningProperty());
        task.stateProperty().addListener(new ChangeListener<Worker.State>(){

            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue,
                                Worker.State oldState,
                                Worker.State newState) {
                if(newState == Worker.State.SUCCEEDED) {
                    try {
                        handleBadgeScanSuccess(task.get());
                    } catch (Exception e) {
                        gLog.error("Badge scan task failed to complete", e);
                        handleBadgeScanFail();
                    }
                } else if(newState == Worker.State.FAILED) {
                    gLog.warn("Badge scan task failed");
                    handleBadgeScanFail();
                }
            }
        });

        new Thread(task).start();
    }

    private void handleBadgeScanFail() {
        errorMessage.setText(BAD_SCAN_MSG);
    }

    private void handleBadgeScanSuccess(Optional<String> username) {
        if(username.isPresent()) {
            gLog.info("LoginController: Found username {}", username.get());
            user.setUsername(username.get());
            MessageBus.sendMessage(new UserLoginEvent(user));
        } else {
            gLog.info("LoginController: Could not find username from badge");
            errorMessage.setText(BAD_SCAN_MSG);
        }
    }
}
