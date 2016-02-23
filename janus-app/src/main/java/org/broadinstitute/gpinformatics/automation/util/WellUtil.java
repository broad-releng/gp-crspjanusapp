package org.broadinstitute.gpinformatics.automation.util;

public class WellUtil {
    public static String From96To384(String well) {
        int row = well.charAt(0) - 64;
        int col = Integer.parseInt(well.substring(1));
        int newCol = (col * 2) - 1;
        int newRow = (row * 2) - 1;
        String newRowAscii = String.valueOf((char)(newRow + 64));
        return newRowAscii + newCol;
    }

    //Format A1 to A01
    public static String Format(String well) {
        String row = String.valueOf(well.charAt(0));
        int col = Integer.parseInt(well.substring(1));
        if(col < 10)
            return String.format("%s0%d", row, col);
        else
            return well;
    }
}
