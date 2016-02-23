package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;

import java.util.List;

/**
 * Decorator over WorklistBuilder to add Validations
 */
public abstract class WorklistBuilderDecorator implements WorklistBuilder {
    private WorklistBuilder worklistBuilder;

    protected WorklistBuilderDecorator(WorklistBuilder worklistBuilder) {
        this.worklistBuilder = worklistBuilder;
    }

    @Override
    public List<WorklistRow> build() {
        return worklistBuilder.build();
    }
}
