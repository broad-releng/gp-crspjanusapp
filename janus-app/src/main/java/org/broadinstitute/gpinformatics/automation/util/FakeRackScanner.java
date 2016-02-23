package org.broadinstitute.gpinformatics.automation.util;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Built to demo app
 */
public class FakeRackScanner implements RackScanner {
    private static final Logger gLog = LoggerFactory.getLogger(FakeRackScanner.class);
    public static final String NOREAD = "NOREAD";
    private final File file;

    public FakeRackScanner(File file) {
        this.file = file;
    }

    @Override
    public Rack fire() throws TubeDecodeException {
        DatapaqFileReader fileReader = new DatapaqFileReader();
        try {
            Rack rack = new Rack(8, 12);
            List<DatapaqRackScanResult> list = fileReader.read(this.file);
            for(DatapaqRackScanResult line : list){
                int row = line.getRow().charAt(0) - 'A';
                int col = line.getColumn() - 1;
                rack.addTube(row, col, line.getTubeBarcode());
                if(!line.getRackBarcode().startsWith(NOREAD))
                    rack.setBarcode(line.getRackBarcode());
            }

            return rack;
        } catch (IOException e) {
            gLog.error("FakeRackScanner: could not read file", e);
        }
        return null;
    }
}
