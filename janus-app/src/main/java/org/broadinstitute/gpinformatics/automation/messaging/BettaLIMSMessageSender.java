package org.broadinstitute.gpinformatics.automation.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Sends and stores bettalims messages
 */
public class BettaLIMSMessageSender {
    private static final Logger gLog = LoggerFactory.getLogger(BettaLIMSMessageSender.class);
    private File mStashDir;
    private File mArchiveDir;
    private MessageTransport mMessageTransport;
    private boolean mArchiveSentMessages;

    public BettaLIMSMessageSender() {
        mArchiveSentMessages = false;
    }

    /**
     * Send a <code>BettaLIMSMessage</code> xml message to the bettaLIMS messaging system.
     *
     * @param message the <code>Message</code> object which wraps the <code>BettaLIMSMessage</code> object.
     * @return True if message was sent and not stashed
     */
    public boolean sendMessage(Message message) {
        gLog.debug("sendMessage() called");

        if (gLog.isDebugEnabled()) {
            gLog.debug(message.messageToString());
        }

        // send the message
        if(!mMessageTransport.sendMessage(message)){
            message.stashMessage(mStashDir);
            return false;
        }

        if(mArchiveSentMessages){
            message.archiveMessage(mArchiveDir);
        }

        return true;
    }


    public void sendStashedMessages() {
        File[] messageFiles = mStashDir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith("xml");
            }
        });

        Message message;
        for (File messageFile : messageFiles) {
            message = Message.fromFile(messageFile);
            if (message != null && mMessageTransport.sendMessage(message)) {
                messageFile.delete();
            }
        }
    }

    public File getStashDir() {
        return mStashDir;
    }

    @Required
    public void setStashDir(File stashDir) {
        gLog.info("BettaLIMSMessageSender: Settings stashdir " + stashDir.getAbsolutePath());
        mStashDir = stashDir;
        if(!mStashDir.exists()){
            mStashDir.mkdir();
        }
    }

    public void setArchiveDir(File archiveDir) {
        gLog.info("BettaLIMSMessageSender: Settings archivedir " + archiveDir.getAbsolutePath());
        mArchiveDir = archiveDir;
        if(!mArchiveDir.exists()){
            mArchiveDir.mkdir();
        }
    }

    public MessageTransport getMessageTransport() {
        return mMessageTransport;
    }

    @Required
    public void setMessageTransport(MessageTransport messageTransport) {
        mMessageTransport = messageTransport;
    }

    public void setArchiveSentMessages(boolean archiveSentMessages) {
        mArchiveSentMessages = archiveSentMessages;
    }

    public boolean getArchiveSentMessages() {
        return mArchiveSentMessages;
    }
}
