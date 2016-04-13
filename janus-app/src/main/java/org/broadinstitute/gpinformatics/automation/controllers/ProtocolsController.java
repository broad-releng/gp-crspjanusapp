package org.broadinstitute.gpinformatics.automation.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import org.broadinstitute.gpinformatics.automation.protocols.ProtocolTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for JanusProtocols.fxml. Changes the detail view of the stack pane
 * when the user selects a new ProtocolType in the TreeView
 */
public class ProtocolsController extends StackPaneController implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(ProtocolsController.class);

    @FXML
    private TreeView<ProtocolTypes> treeView;

    @FXML
    private Pane fingerprintNormalizationPane;

    @FXML
    private Pane shearingNormalizationPane;

    @FXML
    private Pane normalizationPane;

    @FXML
    public Pane raiseVolumePane;

    @FXML
    public Pane truSeqAliquotPane;

    @FXML
    public Pane truSeqSpikePane;

    @FXML
    private Pane rnaCaliperQCPane;

    @FXML
    private Pane volumeTransferPane;

    @FXML
    private Pane sonicDaughterPlateCreationPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        assert treeView != null : "treeView was not injected";
        assert fingerprintNormalizationPane != null : "fingerprintNormalizationPane was not injected";
        assert shearingNormalizationPane != null : "shearingNormalizationPane was not injected";
        assert raiseVolumePane != null : "raiseVolumePane was not injected";
        assert truSeqAliquotPane != null : "truSeqAliquotPane was not injected";
        assert truSeqSpikePane != null : "truSeqSpikePane was not injected";
        assert rnaCaliperQCPane != null : "rnaCaliperQCPane was not injected";
        assert volumeTransferPane != null : "volumeTransferPane was not injected";
        assert sonicDaughterPlateCreationPane != null : "sonicDaughterPlateCreationPane was not injected";

        initializeTreeView();

        replaceContent(fingerprintNormalizationPane);
    }

    private void initializeTreeView() {
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ProtocolTypes>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<ProtocolTypes>> observableValue,
                                TreeItem<ProtocolTypes> oldValue,
                                TreeItem<ProtocolTypes> newValue) {
                switch (newValue.getValue()) {
                case FingerprintNormalization:
                    replaceContent(fingerprintNormalizationPane);
                    break;
                case ShearingNormalization:
                    replaceContent(shearingNormalizationPane);
                    break;
                case Normalization:
                    replaceContent(normalizationPane);
                    break;
                case RaiseVolume:
                    replaceContent(raiseVolumePane);
                    break;
                case TruSeqAliquot:
                    replaceContent(truSeqAliquotPane);
                    break;
                case TruSeqSpike:
                    replaceContent(truSeqSpikePane);
                    break;
                case RNACaliperQC:
                    replaceContent(rnaCaliperQCPane);
                    break;
                case VolumeTransfer:
                    replaceContent(volumeTransferPane);
                    break;
                case SonicDaughterPlateCreation:
                    replaceContent(sonicDaughterPlateCreationPane);
                    break;
                }
            }
        });
    }
}
