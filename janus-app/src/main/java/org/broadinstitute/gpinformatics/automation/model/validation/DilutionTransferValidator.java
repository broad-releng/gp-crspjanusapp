package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.Arrays;
import java.util.List;

/**
 * A Dilution transfer must be OneToOne, have different rack barcodes,
 * and Source rack should be known in lims.
 */
public class DilutionTransferValidator extends  TransferValidatorBase {
    private final Rack source;
    private final Rack destination;
    private final WorklistBuilder worklistBuilder;
    private final LimsService limsService;

    public DilutionTransferValidator(
            Rack source,
            Rack destination,
            WorklistBuilder worklistBuilder,
            LimsService limsService) {
        super(worklistBuilder);
        this.source = source;
        this.destination = destination;
        this.worklistBuilder = worklistBuilder;
        this.limsService = limsService;
    }

    @Override
    protected List<Validation> getValidations() {
        return Arrays.asList(
                new RackValidation(source),
                new RackValidation(destination),
                new SourceRackValidation(source, limsService),
                new OneToOneValidation(source, destination),
                new UniqueBarcodesValidation(source, destination));
    }

    @Override
    public List<WorklistRow> build() {
        validate();
        return worklistBuilder.build();
    }
}
