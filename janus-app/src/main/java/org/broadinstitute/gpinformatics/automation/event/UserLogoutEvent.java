package org.broadinstitute.gpinformatics.automation.event;

import com.google.common.base.MoreObjects;
import org.broadinstitute.gpinformatics.automation.model.User;

/**
 * Message bus event to signify that the user has logged out of the application
 */
public class UserLogoutEvent {
    private User user;

    public UserLogoutEvent(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .toString();
    }
}
