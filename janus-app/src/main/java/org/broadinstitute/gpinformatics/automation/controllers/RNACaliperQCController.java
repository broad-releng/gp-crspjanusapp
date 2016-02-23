package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.broadinstitute.gpinformatics.automation.layout.PlatePane;
import org.broadinstitute.gpinformatics.automation.layout.RackPane;
import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WellTransferRow;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.model.validation.RNACaliperQCTransferValidator;
import org.broadinstitute.gpinformatics.automation.util.BettalimsMessageFactory;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.worklist.RNACaliperQCWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateTransferEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReagentType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReceptacleType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Controller that handles the RNA Caliper QC fxml view
 */
public class RNACaliperQCController extends BaseProtocolController {
    private static final Logger gLog = LoggerFactory.getLogger(RNACaliperQCController.class);

    private final static String DEFAULT_CONC = "5";

    @FXML
    public TextField targetConcentrationTextField;

    @FXML
    public TextField targetVolumeTextField;

    @FXML
    public Button transferButton;

    @FXML
    public RackPane sourceRackPane;

    @FXML
    public PlatePane destinationPlatePane;

    @Autowired
    public User user;

    @Autowired
    public MachineNameLookup machineNameLookup;

    @Autowired
    private LimsService limsService;

    private TargetConcentration targetConcentration;

    private Rack sourceRack;
    private Plate destinationPlate;

    @Override
    public WorklistBuilder doOnInitialized() {
        assert sourceRackPane != null : "sourceRackPane was not injected by FXML";
        assert destinationPlatePane != null : "destinationPlatePane was not injected by FXML";
        assert targetConcentrationTextField != null : "targetConcentrationTextField was not injected by FXML";
        assert targetVolumeTextField != null : "targetVolumeTextField was not injected by FXML";
        assert transferButton != null : "transferButton was not injected by FXML";

        //Bind Form
        targetConcentration = new TargetConcentration();
        targetConcentrationTextField.textProperty()
                .bindBidirectional(targetConcentration.targetConcentrationProperty());
        targetConcentration.setTargetConcentration(DEFAULT_CONC);
        transferButton.disableProperty().bind(
                targetConcentration.isValid());

        setSourceRack(sourceRackPane.getRack());
        setDestinationPlate(destinationPlatePane.getPlate());

        reagent.setReagentType("Sample Buffer");

        RNACaliperQCWorklistBuilder worklistBuilder = new RNACaliperQCWorklistBuilder(
                getSourceRack(), getDestinationPlate(), targetConcentration, limsService);
        return new RNACaliperQCTransferValidator(
                getSourceRack(), getDestinationPlate(), worklistBuilder, limsService);
    }

    @Override
    public void handleReset() {
        sourceRackPane.getRack().reset();
        destinationPlate.reset();
        targetConcentration.reset();
    }

    @Override
    public void callBuildSuccess() {
        super.callBuildSuccess();
        for(WorklistRow row: getWorklist()) {
            WellTransferRow transfer = (WellTransferRow) row;
            destinationPlate.setSampleAtWell(transfer.getDestinationWell());
        }
    }

    @Override
    public BettaLIMSMessage buildBettalimsMessage() throws DatatypeConfigurationException {
        Rack sourceRack = getSourceRack();
        Plate destinationPlate = getDestinationPlate();

        BettaLIMSMessage message = new BettaLIMSMessage();

        PlateType sourcePlate = BettalimsMessageFactory.buildRack(sourceRack);
        PlateType destinationPlateType = BettalimsMessageFactory.buildPlate(destinationPlate);
        destinationPlateType.setSection("P384_96TIP_1INTERVAL_A1");

        List<WorklistRow> worklists = getWorklist();
        List<Double> sourceVolumes = new ArrayList<>();
        List<Double> sourceConcentrations = new ArrayList<>();
        List<String> sourceTubeBarcodes = new ArrayList<>();
        List<String> sourceWells = new ArrayList<>();
        List<Double> destVolumes = new ArrayList<>();
        List<Double> destConcentrations = new ArrayList<>();
        List<String> destWells = new ArrayList<>();
        for(WorklistRow row: worklists) {
            WellTransferRow transfer = (WellTransferRow) row;
            double sourceV2 = transfer.getNewSourceVolume();
            double c1 = transfer.getSourceConcentration();
            String well = transfer.getSourceWell();
            String barcode = transfer.getSourceBarcode();
            sourceVolumes.add(sourceV2);
            sourceConcentrations.add(c1);
            sourceTubeBarcodes.add(barcode);
            sourceWells.add(well);
            destWells.add(transfer.getDestinationWell());
            destVolumes.add(transfer.getDestinationVolume());
            destConcentrations.add(transfer.getDestinationconcentration());
        }

        PositionMapType sourcePositionMap = BettalimsMessageFactory.buildPositionMap(
                sourceRack.getBarcode(), sourceTubeBarcodes, sourceWells, sourceVolumes, sourceConcentrations);

        PositionMapType destPositionMap = BettalimsMessageFactory.buildPositionMap(
                destinationPlate.getBarcode(), null, destWells, destVolumes, destConcentrations);
        for(ReceptacleType receptacleType: destPositionMap.getReceptacle()) {
            receptacleType.setReceptacleType("Plate384WellClear50");
        }

        PlateTransferEventType plateTransferEventType = new PlateTransferEventType();
        plateTransferEventType.setSourcePositionMap(sourcePositionMap);
        plateTransferEventType.setSourcePlate(sourcePlate);
        plateTransferEventType.setPlate(destinationPlateType);
        plateTransferEventType.setPositionMap(destPositionMap);
        plateTransferEventType.setOperator(user.getUsername());
        try {
            plateTransferEventType.setStation(machineNameLookup.getMachineName());
        } catch (IOException e) {
            gLog.error("PlateTransferEventController: couldn't grab machine name", e);
            throw new RuntimeException(e); //Should have blown up earlier on startup
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        plateTransferEventType.setEventType(protocolType.getEventType());
        plateTransferEventType.setProgram("Janus");
        plateTransferEventType.setStart(date);
        plateTransferEventType.setEnd(date);
        plateTransferEventType.setDisambiguator(1L);

        ReagentType reagentType = BettalimsMessageFactory.buildReagent(
                reagent.getReagentType(), reagent.getBarcode(), reagent.getExpirationDate());

        plateTransferEventType.getReagent().add(reagentType);

        message.getPlateTransferEvent().add(plateTransferEventType);
        return message;
    }

    public void setSourceRack(Rack sourceRack) {
        this.sourceRack = sourceRack;
    }

    public Rack getSourceRack() {
        if(sourceRack == null)
            sourceRack = sourceRackPane.getRack();
        return sourceRack;
    }

    public Plate getDestinationPlate() {
        if(destinationPlate == null)
            destinationPlate = destinationPlatePane.getPlate();
        return destinationPlate;
    }

    public void setDestinationPlate(Plate destinationPlate) {
        this.destinationPlate = destinationPlate;
    }
}
