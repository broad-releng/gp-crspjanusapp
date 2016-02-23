package org.broadinstitute.gpinformatics.automation.util;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.broadinstitute.gpinformatics.automation.App;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 * Lookup machine name stored in init file
 */
public class MachineNameLookup {

    private String machineName;

    @Autowired
    public RootConfig rootConfig;

    public String getMachineName() throws IOException {
        if(App.UI_TEST)
            machineName = App.machineName;
        else if(machineName == null) {
            File initliazationFile = new File(rootConfig.getRootDirectory(), "super_init.json");
            Gson gson = new Gson();
            String json = FileUtils.readFileToString(initliazationFile);
            InitializationFileModel initializationFileModel =
                    gson.fromJson(json, InitializationFileModel.class);
            machineName = initializationFileModel.getMachineName();
        }
        return machineName;
    }

    public void setRootConfig(RootConfig rootConfig) {
        this.rootConfig = rootConfig;
    }

    public static class InitializationFileModel {
        public String machineName;

        //For gson binding
        public InitializationFileModel() {
        }

        public String getMachineName() {
            return machineName;
        }

        public void setMachineName(String machineName) {
            this.machineName = machineName;
        }
    }
}
