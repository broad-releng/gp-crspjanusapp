package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a logged in user, empty string if no one logged in
 */
public class User {
    private StringProperty username = new SimpleStringProperty("");

    public User() {
    }

    public User(String username) {
        setUsername(username);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    @Override
    public String toString() {
        return "User{" +
               "username=" + username +
               '}';
    }
}
