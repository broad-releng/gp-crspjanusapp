package org.broadinstitute.gpinformatics.automation;


import jfxtras.labs.dialogs.MonologFX;

/**
 * Utility class to easily display dialogs
 */
public class Alerts {
    public static void error(String msg) {
        MonologFX dialog = new MonologFX(MonologFX.Type.ERROR);
        dialog.setTitleText("Error!");
        dialog.setMessage(msg);
        dialog.show();
    }

    public static void info(String msg) {
        MonologFX dialog = new MonologFX(MonologFX.Type.INFO);
        dialog.setTitleText("Info:");
        dialog.setMessage(msg);
        dialog.show();
    }

    public static void success(String msg) {
        MonologFX dialog = new MonologFX(MonologFX.Type.ACCEPT);
        dialog.setTitleText("Success");
        dialog.setMessage(msg);
        dialog.show();
    }
}
