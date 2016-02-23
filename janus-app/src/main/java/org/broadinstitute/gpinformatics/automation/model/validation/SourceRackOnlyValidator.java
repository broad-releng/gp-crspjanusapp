package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.Arrays;
import java.util.List;

/**
 * Calls validators that check if Rack is setup and tubes are known in lims
 */
public class SourceRackOnlyValidator extends TransferValidatorBase {

    private final WorklistBuilder worklistBuilder;
    private Rack rack;
    private LimsService limsService;

    public SourceRackOnlyValidator(WorklistBuilder worklistBuilder,
                                   Rack rack, LimsService limsService) {
        super(worklistBuilder);
        this.worklistBuilder = worklistBuilder;
        this.rack = rack;
        this.limsService = limsService;
    }

    @Override
    protected List<Validation> getValidations() {
        return Arrays.asList(new RackValidation(rack),
                new SourceRackValidation(rack, limsService));
    }

    @Override
    public List<WorklistRow> build() {
        validate();
        return worklistBuilder.build();
    }
}
