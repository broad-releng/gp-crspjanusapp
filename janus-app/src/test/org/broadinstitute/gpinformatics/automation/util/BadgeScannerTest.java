package org.broadinstitute.gpinformatics.automation.util;

import com.google.common.base.Optional;
import junit.framework.Assert;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.Test;

import java.io.File;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BadgeScannerTest {
    @Test
    public void testGetUsernameUnknown() throws Exception {
        File badgeScanFile = new File("exe");
        RuntimeExecutor executor = mock(RuntimeExecutor.class);
        when(executor.execute(anyString())).thenReturn("UNKNOWN");
        BadgeScanner badgeScanner = new BadgeScanner(executor, badgeScanFile);
        Optional<String> username = badgeScanner.getUsernameFromBadgeScan();
        Assert.assertFalse(username.isPresent());
    }

    @Test
    public void testGetUsername() throws Exception {
        File badgeScanFile = new File("exe");
        RuntimeExecutor executor = mock(RuntimeExecutor.class);
        when(executor.execute(anyString())).thenReturn("myuid");
        LimsService limsService = mock(LimsService.class);
        when(limsService.fetchUserNameFromBadgeId("myuid")).thenReturn("jowalsh");
        BadgeScanner badgeScanner = new BadgeScanner(executor, badgeScanFile);
        badgeScanner.limsService = limsService;
        Optional<String> username = badgeScanner.getUsernameFromBadgeScan();
        Assert.assertTrue(username.isPresent());
    }
}
