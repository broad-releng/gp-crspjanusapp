package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.RNACaliperQCWorklistBuilder;

import java.util.Arrays;
import java.util.List;
import org.broadinstitute.techdev.lims.mercury.*;

/**
 * An RNA transfer must be from A1 of 96 to quad 1 of 384 and source
 * rack should have known concentrations
 */
public class RNACaliperQCTransferValidator extends TransferValidatorBase {
    private final Rack sourceRack;
    private final Plate destinationPlate;
    private final RNACaliperQCWorklistBuilder worklistBuilder;
    private final LimsService limsService;

    public RNACaliperQCTransferValidator(Rack sourceRack, Plate destinationPlate,
                                         RNACaliperQCWorklistBuilder worklistBuilder, LimsService limsService) {
        super(worklistBuilder);
        this.sourceRack = sourceRack;
        this.destinationPlate = destinationPlate;
        this.worklistBuilder = worklistBuilder;
        this.limsService = limsService;
    }

    @Override
    protected List<Validation> getValidations() {
        return Arrays.asList(
                new RackValidation(sourceRack),
                new SourceRackValidation(sourceRack, limsService),
                new BarcodeValidation(sourceRack.getBarcode(), destinationPlate.getBarcode()));
    }

    @Override
    public List<WorklistRow> build() {
        validate();
        return worklistBuilder.build();
    }
}
