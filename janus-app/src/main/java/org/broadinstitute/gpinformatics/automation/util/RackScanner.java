package org.broadinstitute.gpinformatics.automation.util;

import org.broadinstitute.gpinformatics.automation.model.Rack;

public interface RackScanner {
    public Rack fire() throws TubeDecodeException;
}
