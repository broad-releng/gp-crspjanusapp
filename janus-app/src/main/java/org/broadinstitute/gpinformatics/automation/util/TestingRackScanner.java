package org.broadinstitute.gpinformatics.automation.util;

import org.broadinstitute.gpinformatics.automation.model.Rack;

/**
 * Just for local testing
 */
public class TestingRackScanner implements RackScanner {
    @Override
    public Rack fire() throws TubeDecodeException {
        Rack rack = new Rack(8,12);
        rack.addTube(0,0,"0175362242");
        rack.addTube(0,1,"0175362313");
        rack.addTube(0,2,"0175362307");
        rack.addTube(0,3,"0175362317");
        rack.addTube(0,4,"0175362287");
        rack.addTube(1,4,"0175362333");
        rack.addTube(1,3,"0175362244");
        rack.addTube(1,2,"0175362269");
        rack.addTube(1,1,"0175362259");
        rack.addTube(1,0,"0175362278");
        return rack;
    }
}