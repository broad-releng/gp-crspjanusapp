package org.broadinstitute.gpinformatics.automation.controllers;

/**
 * Determines what unrequired messaging attributes should a message contain
 */
public final class MessageAttributeTypes {
    public enum IncludeConcentration {
        TRUE(true),
        FALSE(false);
        private boolean value;

        private IncludeConcentration(boolean value) {
            this.value = value;
        }
    }

    public enum IncludeVolume {
        TRUE(true),
        FALSE(false);
        private boolean value;

        private IncludeVolume(boolean value) {
            this.value = value;
        }
    }
}
