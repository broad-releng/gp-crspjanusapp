package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import org.broadinstitute.gpinformatics.automation.layout.RackPane;
import org.broadinstitute.gpinformatics.automation.model.GenericTransfer;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.util.BettalimsMessageFactory;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateTransferEventType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReagentType;
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
 * For providing a base implementation of a PlateTransferEvent
 */
public abstract class PlateTransferEventController extends BaseProtocolController {
    private static final Logger gLog = LoggerFactory.getLogger(PlateTransferEventController.class);

    @FXML
    public RackPane sourceRackPane;

    @FXML
    public RackPane destinationRackPane;

    @Autowired
    public User user;

    @Autowired
    public MachineNameLookup machineNameLookup;

    private Rack sourceRack;

    private Rack destinationRack;

    @Override
    public BettaLIMSMessage buildBettalimsMessage() throws DatatypeConfigurationException {
        Rack sourceRack = getSourceRack();
        Rack destinationRack = getDestinationRack();

        BettaLIMSMessage message = new BettaLIMSMessage();

        PlateType sourcePlate = BettalimsMessageFactory.buildRack(sourceRack);
        PlateType destinationPlate = BettalimsMessageFactory.buildRack(destinationRack);

        List<WorklistRow> worklists = getWorklist();
        List<Double> sourceVolumes = new ArrayList<>();
        List<String> sourceTubeBarcodes = new ArrayList<>();
        List<String> sourceWells = new ArrayList<>();
        List<Double> destVolumes = new ArrayList<>();
        List<Double> destConcentrations = new ArrayList<>();
        List<String> destTubeBarcodes = new ArrayList<>();
        List<String> destWells = new ArrayList<>();
        for(WorklistRow row: worklists) {
            GenericTransfer transfer = (GenericTransfer) row;
            double sourceV2 = transfer.getNewSourceVolume();
            double c1 = transfer.getSourceConcentration();
            String well = transfer.getSourceWell();
            String barcode = transfer.getSourceBarcode();
            sourceVolumes.add(sourceV2);
            sourceTubeBarcodes.add(barcode);
            sourceWells.add(well);
            destWells.add(transfer.getDestinationWell());
            destVolumes.add(transfer.getDestinationVolume());
            destConcentrations.add(transfer.getDestinationConcentration());
            destTubeBarcodes.add(transfer.getDestinationBarcode());
        }

        PositionMapType sourcePositionMap = BettalimsMessageFactory.buildPositionMap(
                sourceRack.getBarcode(), sourceTubeBarcodes, sourceWells, sourceVolumes);

        PositionMapType positionMap = BettalimsMessageFactory.buildPositionMap(
                destinationRack.getBarcode(), destTubeBarcodes, destWells, destVolumes, destConcentrations);

        PlateTransferEventType plateTransferEventType = new PlateTransferEventType();
        plateTransferEventType.setSourcePositionMap(sourcePositionMap);
        plateTransferEventType.setSourcePlate(sourcePlate);
        plateTransferEventType.setPositionMap(positionMap);
        plateTransferEventType.setPlate(destinationPlate);
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

        if(reagent != null) {
            ReagentType reagentType = BettalimsMessageFactory.buildReagent(
                    reagent.getReagentType(), reagent.getBarcode(), reagent.getExpirationDate());

            plateTransferEventType.getReagent().add(reagentType);
        }

        message.getPlateTransferEvent().add(plateTransferEventType);
        return message;
    }

    public void setSourceRack(Rack sourceRack) {
        this.sourceRack = sourceRack;
    }

    public void setDestinationRack(Rack destinationRack) {
        this.destinationRack = destinationRack;
    }

    public Rack getSourceRack() {
        if(sourceRack == null)
            sourceRack = sourceRackPane.getRack();
        return sourceRack;
    }

    public Rack getDestinationRack() {
        if(destinationRack == null)
            destinationRack = destinationRackPane.getRack();
        return destinationRack;
    }
}
