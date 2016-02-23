package org.broadinstitute.gpinformatics.automation.model.validation;

import javafx.beans.property.DoubleProperty;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.Arrays;
import java.util.List;

/**
 * Spikes will require parents. All transfers will require daughter rack tubes
 */
public class NormalizationAndSpikeTransferValidator extends  TransferValidatorBase {
    private final Rack source;
    private final Rack destination;
    private final DoubleProperty spikeConcentrationMax;
    private final WorklistBuilder worklistBuilder;
    private final LimsService limsService;

    public NormalizationAndSpikeTransferValidator(
            Rack source,
            Rack destination,
            DoubleProperty spikeConcentrationMax, WorklistBuilder worklistBuilder,
            LimsService limsService) {
        super(worklistBuilder);
        this.source = source;
        this.destination = destination;
        this.spikeConcentrationMax = spikeConcentrationMax;
        this.worklistBuilder = worklistBuilder;
        this.limsService = limsService;
    }

    @Override
    protected List<Validation> getValidations() {
        return Arrays.asList(
                new RackValidation(destination),
                new SpikeRequiresParentsValidation(source, destination, spikeConcentrationMax, limsService));
    }

    @Override
    public List<WorklistRow> build() {
        validate();
        return worklistBuilder.build();
    }
}
