package org.broadinstitute.gpinformatics.automation.util;

import org.broadinstitute.gpinformatics.automation.model.Rack;

/**
 * Built to demo app
 */
public class MockRackScanner implements RackScanner {
    public static int COUNTER = 0;

    @Override
    public Rack fire() throws TubeDecodeException {
        Rack rack = new Rack();
        if(COUNTER == 0) {
            rack.addTube(0, 0, "201410011254071"); //14 at 69
            rack.setBarcode(String.valueOf(COUNTER++));
        } else {
            rack.addTube(0, 0, "0FAKE00555444");
            rack.setBarcode(String.valueOf(COUNTER));
        }
        return rack;
    }
}
