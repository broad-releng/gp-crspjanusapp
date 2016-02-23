package org.broadinstitute.gpinformatics.automation.util;

import junit.framework.Assert;
import org.broadinstitute.gpinformatics.automation.JanusAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Test machine name lookup
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = JanusAppConfig.class)
public class MachineNameLookupTest {
    @Autowired
    public MachineNameLookup machineNameLookup;

    @Test
    public void testGetMachineName() throws Exception {
        String machineName = machineNameLookup.getMachineName();
        Assert.assertEquals("BATMAN", machineName);
    }

    @Test(expected = IOException.class)
    public void testGetMachineNameNoInitFile() throws Exception {
        MachineNameLookup machineNameLookup = new MachineNameLookup();
        RootConfig rootConfig = new RootConfig("FAKEDIR");
        machineNameLookup.setRootConfig(rootConfig);
        machineNameLookup.getMachineName();
    }
}
