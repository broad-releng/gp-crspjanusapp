package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.WorklistRow;

import java.util.List;

/**
 * Base definition of building a Janus Worklist
 */
public interface WorklistBuilder {
    public List<WorklistRow> build();
}
