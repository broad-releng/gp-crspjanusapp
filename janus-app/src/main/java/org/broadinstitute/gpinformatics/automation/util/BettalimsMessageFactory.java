package org.broadinstitute.gpinformatics.automation.util;


import org.broadinstitute.gpinformatics.automation.controllers.MessageAttributeTypes;
import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PlateType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.PositionMapType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReagentType;
import org.broadinstitute.gpinformatics.mercury.bettalims.generated.ReceptacleType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility for building parts of a bettalims message
 */
public class BettalimsMessageFactory {

    public static PlateType buildRack(Rack rack) {
        PlateType plateType = new PlateType();
        plateType.setBarcode(rack.getBarcode());
        plateType.setPhysType("TubeRack");
        plateType.setSection("ALL96");
        return plateType;
    }

    public static PositionMapType buildPositionMap(String barcode, List<String> barcodes, List<String> wells,
                                                   List<Double> volumes,
                                                   List<Double> concentrations) {
        return buildPositionMap(barcode, barcodes, wells, volumes, concentrations,
                MessageAttributeTypes.IncludeConcentration.TRUE, MessageAttributeTypes.IncludeVolume.TRUE);
    }

    public static PositionMapType buildPositionMap(String barcode, List<String> barcodes,
                                                   List<String> wells,
                                                   List<Double> volumes) {
        return buildPositionMap(barcode, barcodes, wells, volumes, null,
                MessageAttributeTypes.IncludeConcentration.FALSE, MessageAttributeTypes.IncludeVolume.TRUE);
    }

    public static ReagentType buildReagent(String kitType, String barcode, Calendar expirationDate)
            throws DatatypeConfigurationException {
        ReagentType reagentType = new ReagentType();
        reagentType.setBarcode(barcode);
        reagentType.setKitType(kitType);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(expirationDate.getTime());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        reagentType.setExpiration(date);
        return reagentType;
    }

    public static PositionMapType buildPositionMap(String barcode, List<String> barcodes, List<String> wells,
                                                   List<Double> volumes,
                                                   List<Double> concentrations,
                                                   MessageAttributeTypes.IncludeConcentration includeConcentration,
                                                   MessageAttributeTypes.IncludeVolume includeVolume) {
        PositionMapType positionMapType = new PositionMapType();
        positionMapType.setBarcode(barcode);
        for(int i = 0; i < wells.size(); i++) {
            ReceptacleType receptacle = new ReceptacleType();
            if(barcodes != null)
                receptacle.setBarcode(barcodes.get(i));
            receptacle.setPosition(wells.get(i));
            if(includeConcentration == MessageAttributeTypes.IncludeConcentration.TRUE) {
                BigDecimal conc = BigDecimal.valueOf(concentrations.get(i));
                conc = conc.setScale(2, BigDecimal.ROUND_HALF_UP);
                receptacle.setConcentration(conc);
            }
            if(includeVolume == MessageAttributeTypes.IncludeVolume.TRUE) {
                BigDecimal vol = BigDecimal.valueOf(volumes.get(i));
                vol = vol.setScale(2, BigDecimal.ROUND_HALF_UP);
                receptacle.setVolume(vol);
            }
            receptacle.setReceptacleType("tube");
            positionMapType.getReceptacle().add(receptacle);
        }

        return positionMapType;
    }

    public static PlateType buildPlate(Plate destinationPlate) {
        PlateType plateType = new PlateType();
        plateType.setBarcode(destinationPlate.getBarcode());
        plateType.setPhysType(destinationPlate.getPhysType());
        plateType.setSection("ALL384");
        return plateType;
    }
}