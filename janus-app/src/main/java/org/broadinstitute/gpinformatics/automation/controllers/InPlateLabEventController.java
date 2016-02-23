package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.broadinstitute.gpinformatics.automation.layout.RackPane;
import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.util.BettalimsMessageFactory;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateEventType;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

/**
 * For providing a base implementation of a PlateEvent
 */
public abstract class InPlateLabEventController extends BaseProtocolController implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(InPlateLabEventController.class);
    private final MessageAttributeTypes.IncludeConcentration includeConcentration;
    private final MessageAttributeTypes.IncludeVolume includeVolume;

    @FXML
    public RackPane rackPane;

    @Autowired
    public User user;

    @Autowired
    public MachineNameLookup machineNameLookup;

    private Rack rack;

    protected InPlateLabEventController(
            MessageAttributeTypes.IncludeConcentration includeConcentration, MessageAttributeTypes.IncludeVolume includeVolume) {
        this.includeConcentration = includeConcentration;
        this.includeVolume = includeVolume;
    }

    @Override
    public BettaLIMSMessage buildBettalimsMessage() throws DatatypeConfigurationException {
        rack = getRack();

        BettaLIMSMessage message = new BettaLIMSMessage();

        PlateType plate = BettalimsMessageFactory.buildRack(rack);

        List<WorklistRow> worklists = getWorklist();
        List<Double> volumes = new ArrayList<>();
        List<Double> concentrations = new ArrayList<>();
        List<String> barcodes = new ArrayList<>();
        List<String> wells = new ArrayList<>();
        for(WorklistRow row: worklists) {
            Normalization normalization = (Normalization) row;
            double v2 = normalization.getV2();
            double c2 = normalization.getC2();
            String well = normalization.getWell();
            String barcode = normalization.getBarcode();
            volumes.add(v2);
            concentrations.add(c2);
            barcodes.add(barcode);
            wells.add(well);
        }

        PositionMapType positionMap = BettalimsMessageFactory.buildPositionMap(
                rack.getBarcode(), barcodes, wells, volumes, concentrations, includeConcentration, includeVolume);

        PlateEventType plateEventType = new PlateEventType();
        plateEventType.setPositionMap(positionMap);
        plateEventType.setPlate(plate);
        plateEventType.setOperator(user.getUsername());
        try {
            plateEventType.setStation(machineNameLookup.getMachineName());
        } catch (IOException e) {
            gLog.error("PlateTransferEventController: couldn't grab machine name", e);
            throw new RuntimeException(e); //Should have blown up earlier on startup
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        plateEventType.setEventType(protocolType.getEventType());
        plateEventType.setProgram("Janus");
        plateEventType.setStart(date);
        plateEventType.setEnd(date);
        plateEventType.setDisambiguator(1L);

        ReagentType reagentType = BettalimsMessageFactory.buildReagent(
                reagent.getReagentType(), reagent.getBarcode(), reagent.getExpirationDate());

        plateEventType.getReagent().add(reagentType);

        message.getPlateEvent().add(plateEventType);

        return message;
    }

    public void setRack(Rack sourceRack) {
        this.rack = sourceRack;
    }

    public Rack getRack() {
        if(rack == null)
            rack = rackPane.getRack();
        return rack;
    }
}
