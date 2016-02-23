package org.broadinstitute.gpinformatics.automation.util;

import com.google.common.base.Optional;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Handles Badge reading commands
 */
public class BadgeScanner {
    private static final Logger gLog = LoggerFactory.getLogger(BadgeScanner.class);
    public static final String UNKNOWN_USER_RESPONSE = "UNKNOWN";
    private File badgeScannerExecutable;
    private RuntimeExecutor executor;

    @Autowired
    public LimsService limsService;

    public BadgeScanner(RuntimeExecutor executor, File badgeScannerExecutable) {
        this.executor = executor;
        this.badgeScannerExecutable = badgeScannerExecutable;
    }

    /**
     * Calls exe to get UID. Asks Lims to translate from UID to username
     *
     * @return - Some username if found, else absent
     * @throws IOException - if exe does not exist
     */
    public Optional<String> getUsernameFromBadgeScan() throws IOException {
        String cmd = "cmd.exe /C \"" + badgeScannerExecutable.getPath() + "\"";
        try {
            String uid = executor.execute(cmd);
            gLog.info("BadgeScanner: output {}", uid);
            if(!uid.equalsIgnoreCase(UNKNOWN_USER_RESPONSE)) {
                String username = limsService.fetchUserNameFromBadgeId(uid);
                return Optional.fromNullable(username);
            }
        } catch (TimeoutException e) {
            gLog.error("BadgeScanner: command timed out", e);
        }

        return Optional.absent();
    }
}
