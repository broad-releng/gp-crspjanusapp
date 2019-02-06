package org.broadinstitute.gpinformatics.automation;

import javafx.application.Application;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Application start point
 */
public class Main {
    public static void main(String[] args) {
        // Bug in El Capitan for Mac OS
        // See https://bugs.openjdk.java.net/browse/JDK-8143907
        try {
            Class<?> macFontFinderClass = Class.forName("com.sun.t2k.MacFontFinder");
            Field psNameToPathMap = macFontFinderClass.getDeclaredField("psNameToPathMap");
            psNameToPathMap.setAccessible(true);
            psNameToPathMap.set(null, new HashMap<String, String>());
        } catch (Exception e) {
            // ignore
        }
        Application.launch(App.class, args);
    }
}
