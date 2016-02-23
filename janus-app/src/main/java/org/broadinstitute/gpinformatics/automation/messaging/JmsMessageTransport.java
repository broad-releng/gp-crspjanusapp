package org.broadinstitute.gpinformatics.automation.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Send bettalims message through JMS
 */
public class JmsMessageTransport implements MessageTransport {
    private static final Logger gLog = LoggerFactory.getLogger(JmsMessageTransport.class);
    private JmsTemplate mJmsTemplate;
    private MessageTransport successor;

    public boolean sendMessage(final Message message) {
        try {
            mJmsTemplate.send(new MessageCreator() {
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(message.messageToString());
                }
            });

            gLog.info("Sent bettaLIMSMessage for: " + message.getIdentifier());
            return true;
        } catch (JmsException ex) {
            gLog.error("JmsMessageTransport: Error sending message.", ex);
        }

        return successor != null && successor.sendMessage(message);
    }

    public JmsTemplate getJmsTemplate() {
        return mJmsTemplate;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        mJmsTemplate = jmsTemplate;
    }

    public void setSuccessor(MessageTransport successor) {
        this.successor = successor;
    }
}
