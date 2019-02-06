package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.TransferType;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.model.validation.NormalizationAndSpikeTransferValidator;
import org.broadinstitute.gpinformatics.automation.util.BettalimsMessageFactory;
import org.broadinstitute.gpinformatics.automation.worklist.NormAndSpikeWorklistBuilder;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReagentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Base implementation of janus protocol that can norm and spike into a rack from its parent container.
 */
public class NormAndSpikesBaseController extends PlateTransferEventController {
    private static final Logger gLog = LoggerFactory.getLogger(NormAndSpikesBaseController.class);

    @FXML
    public TextField belowConcentrationTextField;

    @FXML
    public TextField spikeTargetConcentrationTextField;

    @FXML
    public TextField aboveConcentrationTextField;

    @FXML
    public Button transferButton;

    @FXML
    public TextField normTargetConcentrationTextField;

    @Autowired
    private LimsService limsService;

    private TargetConcentration spikeTargetConcentration;

    private TargetConcentration normalizationConcentration;

    private final DoubleProperty spikeConcentrationMax = new SimpleDoubleProperty();

    private final DoubleProperty normConcentrationMin = new SimpleDoubleProperty();

    @Override
    public WorklistBuilder doOnInitialized() {
        assert sourceRackPane != null : "sourceRackPane was not injected by FXML";
        assert destinationRackPane != null : "destinationRackPane was not injected by FXML";
        assert spikeTargetConcentrationTextField != null : "targetConcentrationTextField was not injected by FXML";
        assert transferButton != null : "transferButton was not injected by FXML";

        //Bind Form
        spikeTargetConcentration = new TargetConcentration();
        spikeTargetConcentrationTextField.textProperty()
                .bindBidirectional(spikeTargetConcentration.targetConcentrationProperty());

        normalizationConcentration = new TargetConcentration();
        normTargetConcentrationTextField.textProperty()
                .bindBidirectional(normalizationConcentration.targetConcentrationProperty());

        StringConverter<Number> converter = new NumberStringConverter();
        belowConcentrationTextField.textProperty().
                bindBidirectional(spikeConcentrationMax, converter);

        aboveConcentrationTextField.textProperty().
                bindBidirectional(normConcentrationMin, converter);

        transferButton.disableProperty().bind(Bindings.and(
                spikeTargetConcentration.isValid(), normalizationConcentration.isValid()));

        setSourceRack(sourceRackPane.getRack());
        setDestinationRack(destinationRackPane.getRack());

        NormAndSpikeWorklistBuilder normAndSpikeWorklistBuilder = new NormAndSpikeWorklistBuilder(
                getSourceRack(), getDestinationRack(), spikeTargetConcentration,
                normalizationConcentration, spikeConcentrationMax,
                normConcentrationMin, limsService);
        return new NormalizationAndSpikeTransferValidator(
                getSourceRack(), getDestinationRack(), spikeConcentrationMax, normAndSpikeWorklistBuilder, limsService);
    }

    @Override
    public void handleReset() {
        sourceRackPane.getRack().reset();
        destinationRackPane.getRack().reset();
        spikeTargetConcentration.reset();
        normalizationConcentration.reset();
        spikeConcentrationMax.setValue(null);
        normConcentrationMin.setValue(null);
    }

    @Override
    public BettaLIMSMessage buildBettalimsMessage() throws DatatypeConfigurationException {
        if(needsSpikes())
            return buildSpikesMessage();
        else
            return buildNormMessage();
    }

    /**
     * Norm events just apply to the daughter samples
     *
     * @return - lims message with a plate event containing V+C updates for daughter rack
     */
    private BettaLIMSMessage buildNormMessage() throws DatatypeConfigurationException {
        Rack daughterRack = getDestinationRack();

        BettaLIMSMessage message = new BettaLIMSMessage();
        PlateType daughterPlate = BettalimsMessageFactory.buildRack(daughterRack);

        List<WorklistRow> worklists = getWorklist();
        List<Double> daughterVolumes = new ArrayList<>();
        List<Double> daughterConcentrations = new ArrayList<>();
        List<String> daughterTubeBarcodes = new ArrayList<>();
        List<String> daughterWells = new ArrayList<>();
        for(WorklistRow row: worklists) {
            NormalizationAndSpikeRow transfer = (NormalizationAndSpikeRow) row;
            daughterVolumes.add(transfer.getNewDaughterVolume());
            daughterConcentrations.add(transfer.getNewDaughterConcentration());
            daughterTubeBarcodes.add(transfer.getDaughterBarcode());
            daughterWells.add(transfer.getDaughterWell());
            if(transfer.getTransferType() == TransferType.SPIKE) {
                throw new RuntimeException("No spikes should be in Norm only message");
            }
        }

        PositionMapType daughterPosMap = BettalimsMessageFactory.buildPositionMap(
                daughterRack.getBarcode(), daughterTubeBarcodes, daughterWells, daughterVolumes, daughterConcentrations);

        PlateEventType daughterPlateEvent = new PlateEventType();
        daughterPlateEvent.setPositionMap(daughterPosMap);
        daughterPlateEvent.setPlate(daughterPlate);
        daughterPlateEvent.setOperator(user.getUsername());

        try {
            daughterPlateEvent.setStation(machineNameLookup.getMachineName());
        } catch (IOException e) {
            gLog.error("PlateTransferEventController: couldn't grab machine name", e);
            throw new RuntimeException(e); //Should have blown up earlier on startup
        }

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        daughterPlateEvent.setEventType(protocolType.getEventType());
        daughterPlateEvent.setProgram("Janus");
        daughterPlateEvent.setStart(date);
        daughterPlateEvent.setEnd(date);
        daughterPlateEvent.setDisambiguator(1L);

        ReagentType reagentType = BettalimsMessageFactory.buildReagent(
                reagent.getReagentType(), reagent.getBarcode(), reagent.getExpirationDate());

        daughterPlateEvent.getReagent().add(reagentType);

        message.getPlateEvent().add(daughterPlateEvent);

        return message;
    }

    /**
     * Spikes require two plate events to set the v+c in lims for both parent
     * and daughter racks
     * @return lims message with two plate events
     */
    private BettaLIMSMessage buildSpikesMessage() throws DatatypeConfigurationException {
        Rack parentRack = getSourceRack();
        Rack daughterRack = getDestinationRack();

        BettaLIMSMessage message = new BettaLIMSMessage();
        PlateType daughterPlate = BettalimsMessageFactory.buildRack(daughterRack);
        PlateType parentPlate = BettalimsMessageFactory.buildRack(parentRack);

        List<WorklistRow> worklists = getWorklist();
        List<Double> daughterVolumes = new ArrayList<>();
        List<Double> daughterConcentrations = new ArrayList<>();
        List<String> daughterTubeBarcodes = new ArrayList<>();
        List<String> daughterWells = new ArrayList<>();
        List<Double> parentVolumes = new ArrayList<>();
        List<Double> parentConcentrations = new ArrayList<>();
        List<String> parentTubeBarcodes = new ArrayList<>();
        List<String> parentWells = new ArrayList<>();
        for(WorklistRow row: worklists) {
            NormalizationAndSpikeRow transfer = (NormalizationAndSpikeRow) row;
            daughterVolumes.add(transfer.getNewDaughterVolume());
            daughterConcentrations.add(transfer.getNewDaughterConcentration());
            daughterTubeBarcodes.add(transfer.getDaughterBarcode());
            daughterWells.add(transfer.getDaughterWell());
            if(transfer.getTransferType() == TransferType.SPIKE) {
                parentWells.add(transfer.getParentWell());
                parentVolumes.add(transfer.getNewParentVolume());
                parentConcentrations.add(transfer.getParentConcentration());
                parentTubeBarcodes.add(transfer.getParentBarcode());
            }
        }

        PositionMapType daughterPosMap = BettalimsMessageFactory.buildPositionMap(
                daughterRack.getBarcode(), daughterTubeBarcodes, daughterWells, daughterVolumes, daughterConcentrations);

        PositionMapType parentPosMap = BettalimsMessageFactory.buildPositionMap(
                parentRack.getBarcode(), parentTubeBarcodes, parentWells, parentVolumes, parentConcentrations);

        PlateEventType daughterPlateEvent = new PlateEventType();
        daughterPlateEvent.setPositionMap(daughterPosMap);
        daughterPlateEvent.setPlate(daughterPlate);
        daughterPlateEvent.setOperator(user.getUsername());

        PlateEventType parentPlateEvent = new PlateEventType();
        parentPlateEvent.setPositionMap(parentPosMap);
        parentPlateEvent.setPlate(parentPlate);
        parentPlateEvent.setOperator(user.getUsername());
        try {
            daughterPlateEvent.setStation(machineNameLookup.getMachineName());
            parentPlateEvent.setStation(machineNameLookup.getMachineName());
        } catch (IOException e) {
            gLog.error("PlateTransferEventController: couldn't grab machine name", e);
            throw new RuntimeException(e); //Should have blown up earlier on startup
        }

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        daughterPlateEvent.setEventType(protocolType.getEventType());
        daughterPlateEvent.setProgram("Janus");
        daughterPlateEvent.setStart(date);
        daughterPlateEvent.setEnd(date);
        daughterPlateEvent.setDisambiguator(1L);

        parentPlateEvent.setEventType(protocolType.getEventType());
        parentPlateEvent.setProgram("Janus");
        parentPlateEvent.setStart(date);
        parentPlateEvent.setEnd(date);
        parentPlateEvent.setDisambiguator(2L);

        ReagentType reagentType = BettalimsMessageFactory.buildReagent(
                reagent.getReagentType(), reagent.getBarcode(), reagent.getExpirationDate());

        daughterPlateEvent.getReagent().add(reagentType);
        parentPlateEvent.getReagent().add(reagentType);

        message.getPlateEvent().add(daughterPlateEvent);
        message.getPlateEvent().add(parentPlateEvent);

        return message;
    }

    private boolean needsSpikes() {
        for(WorklistRow row: getWorklist()) {
            NormalizationAndSpikeRow normalizationAndSpikeRow = (NormalizationAndSpikeRow) row;
            if(normalizationAndSpikeRow.getTransferType() == TransferType.SPIKE)
                return true;
        }
        return false;
    }
}
