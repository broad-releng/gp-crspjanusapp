package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Tube;

/**
 * Racks must have same number of tubes and same layout
 */
public class OneToOneValidation implements Validation {
    private Rack source;
    private Rack destination;

    public OneToOneValidation(Rack source, Rack destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public boolean isValid() {
        if(source == null || destination == null)
            return false;
        if(source.getTubeCount() != destination.getTubeCount())
            return false;
        for(Tube tube: source){
            if(tube.getBarcode() != null){
                if(destination.getTubeAt(tube.getRow(), tube.getColumn()) == null)
                    return false;
            }
        }

        return true;
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Source and Destination tube layouts do not match");
    }
}
