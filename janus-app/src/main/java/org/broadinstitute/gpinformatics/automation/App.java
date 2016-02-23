package org.broadinstitute.gpinformatics.automation;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;

/**
 * Stages Janus App and attaches a global CSS stylesheet
 * Scene dimensions set to full size
 */
public class App extends Application {
    public static final String MACHINE_NAME_PARAM = "machineName";
    public static final String SOURCE_FILE_PARAM = "sourceRackFile";
    public static final String FINGERPRINT_DEST_FILE_PARAM = "fingerprintRackFile";
    public static final String SHEARING_DEST_FILE_PARAM = "shearingRackFile";
    public static final String LIMS_HOSTNAME_PARAM = "hostname";
    public static boolean UI_TEST = false;

    public static File sourceFile;
    public static File fingerprintDestinationFile;
    public static File shearingDestinationFile;
    public static String machineName;
    public static String limsHostname;

    @Override
    public void start(Stage stage) throws Exception {
        Parameters parameters = getParameters();
        Map<String, String> namedParameters = parameters.getNamed();
        if(!namedParameters.isEmpty()) {
            UI_TEST = true;
            machineName = namedParameters.get(MACHINE_NAME_PARAM);
            sourceFile = new File(namedParameters.get(SOURCE_FILE_PARAM));
            fingerprintDestinationFile = new File(namedParameters.get(FINGERPRINT_DEST_FILE_PARAM));
            shearingDestinationFile = new File(namedParameters.get(SHEARING_DEST_FILE_PARAM));
            limsHostname = namedParameters.get(LIMS_HOSTNAME_PARAM);
        }

        SpringFxmlLoader loader = new SpringFxmlLoader();

        Parent root = (Parent) loader.load(
                "/org/broadinstitute/gpinformatics/automation/CrspJanusApplication.fxml");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth()- 20, screenBounds.getHeight() - 20);

        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.sizeToScene();

        stage.show();
    }
}
