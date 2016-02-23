package org.broadinstitute.gpinformatics.automation.event;

import org.broadinstitute.gpinformatics.automation.model.User;

/**
 * Message Bus Event to signify the User has logged in
 */
public class UserLoginEvent {
    private User user;

    public UserLoginEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserLoginEvent{" +
               "user=" + user +
               '}';
    }
}
