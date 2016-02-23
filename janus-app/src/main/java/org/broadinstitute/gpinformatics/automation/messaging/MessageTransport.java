package org.broadinstitute.gpinformatics.automation.messaging;


/**
 * Represents method of sending message
 */
public interface MessageTransport {
    public boolean sendMessage(Message message);
}
