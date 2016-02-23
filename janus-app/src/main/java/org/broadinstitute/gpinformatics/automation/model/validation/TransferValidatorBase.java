package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.worklist.WorklistBuilder;

import java.util.List;

/**
 * abstract validation class that will run through validations
 */
public abstract class TransferValidatorBase extends WorklistBuilderDecorator {
    protected TransferValidatorBase(WorklistBuilder worklistBuilder) {
        super(worklistBuilder);
    }

    protected abstract List<Validation> getValidations();

    protected void validate() {
        for(Validation validation : getValidations()) {
            if(!validation.isValid())
                throw validation.getException();
        }
    }
}
