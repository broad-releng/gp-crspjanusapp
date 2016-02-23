package org.broadinstitute.gpinformatics.automation.util;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Provides support for Datapaq server.exe based rack scans using a given profile
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ZiathRackScanner implements RackScanner {
    private static final Logger gLog = LoggerFactory.getLogger(ZiathRackScanner.class);
    private static final String SERVER_FILE = "\"C:\\Program Files\\ziath\\DataPaq\\server.exe\"";
    public static final String NOREAD = "NOREAD";
    private static final int A_ASCII = 65;
    private final File outputFile;
    private final RuntimeExecutor executor;
    private String profile;
    private final int rows;
    private final int columns;

    public ZiathRackScanner(RuntimeExecutor executor) {
        this(executor, new File("C:\\MESSAGING\\ScanResults"));
    }

    public ZiathRackScanner(RuntimeExecutor executor, File outputDir) {
        this(executor, outputDir, "fuj", 8, 12);
    }

    public ZiathRackScanner(RuntimeExecutor executor, File outputDir,
                            String profile, int rows, int columns) {
        this.executor = executor;
        this.profile = profile;
        this.rows = rows;
        this.columns = columns;
        this.outputFile = new File(outputDir, "ScanResults.txt");
        if(!outputDir.exists())
            outputDir.mkdir();
    }

    /**
     * Returns a Rack after firing datapaq server.exe
     *
     * @return Rack on success. Null on fail.
     */
    @Override
    public Rack fire() throws TubeDecodeException {
        gLog.info("ZiathRackScanner: fire() called");
        deleteOutputFileIfExists();

        String cmd = String.format("cmd.exe /C %s -g %s -f %s", SERVER_FILE, profile, outputFile.getPath());
        gLog.info("ZiathRackScanner: cmd built calling executor {}", cmd);
        try {
            executor.execute(cmd);

            if(!outputFile.exists()){
                gLog.warn("ZiathRackScanner: Output file not created after cmd was run");
                throw new IllegalArgumentException("Could not fire rack scan. Make sure that it is plugged in");
            }

            Rack rack = new Rack(rows, columns);

            DatapaqFileReader reader = new DatapaqFileReader();
            List<DatapaqRackScanResult> list = reader.read(outputFile);

            for(DatapaqRackScanResult line : list){
                int row = line.getRow().charAt(0) - A_ASCII;
                int col = line.getColumn() - 1;
                rack.addTube(row, col, line.getTubeBarcode());
                if(!line.getRackBarcode().startsWith(NOREAD))
                    rack.setBarcode(line.getRackBarcode());
            }

            return rack;
        } catch (IOException e) {
            gLog.error("ZiathRackScanner: error running server.exe");
        } catch (TimeoutException e) {
            gLog.error("ZiathRackScanner: server.exe call timed out");
        }

        return null;
    }

    protected void deleteOutputFileIfExists() {
        if(outputFile.exists())
            outputFile.delete();
    }
}
