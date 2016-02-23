package org.broadinstitute.gpinformatics.automation.messaging;

import org.broadinstitute.gpinformatics.mercury.bettalims.generated.BettaLIMSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Bettalims message wrapper to create file name
 */
public class Message {
    private static final Logger gLog = LoggerFactory.getLogger(Message.class);

    private String mIdentifier;
    private BettaLIMSMessage mBettaLIMSMessage;

    public Message(BettaLIMSMessage bettaLIMSMessage) {
        this(new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()), bettaLIMSMessage);
    }

    private Message(String identifier, BettaLIMSMessage bettaLIMSMessage) {
        mIdentifier = identifier;
        mBettaLIMSMessage = bettaLIMSMessage;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public BettaLIMSMessage getBettaLIMSMessage() {
        return mBettaLIMSMessage;
    }

    public static Message fromFile(File file) {
        Message message = null;
        try {
            String id = file.getName().replace(".xml", "");
            JAXBContext ctx = JAXBContext.newInstance(BettaLIMSMessage.class.getPackage().getName());
            Unmarshaller u = ctx.createUnmarshaller();
            BettaLIMSMessage bMessage = (BettaLIMSMessage) u.unmarshal(new BufferedReader(new FileReader(file)));
            message = new Message(id, bMessage);
        } catch (Exception e) {
            gLog.warn("Error reading stashed message.", e);
        }
        return message;
    }

    public void stashMessage(File stashDirectory) {
        String fileName = mIdentifier + ".xml";
        File file = new File(stashDirectory, fileName);
        if (!file.exists()) {
            try {
                marshal(file);
            } catch (Exception e) {
                gLog.error("Error marshalling message", e);
            }
        }
    }

    public void archiveMessage(File archiveDirectory) {
        String fileName = mIdentifier + ".xml";
        File file = new File(archiveDirectory, fileName);
        if (!file.exists()) {
            try {
                marshal(file);
            } catch (Exception e) {
                gLog.error("Error marshalling message", e);
            }
        }
    }

    public void marshal(File file) throws Exception{
        Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
        JAXBContext ctx = JAXBContext.newInstance(BettaLIMSMessage.class.getPackage().getName());
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(mBettaLIMSMessage,writer);
        writer.close();
    }

    public String messageToString() {
        String toString = null;
        try {
            StringWriter sWriter = new StringWriter();
            JAXBContext ctx = JAXBContext.newInstance(BettaLIMSMessage.class.getPackage().getName());
            Marshaller m = ctx.createMarshaller();
            m.marshal(mBettaLIMSMessage,sWriter);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            toString = sWriter.toString();
        } catch (Exception e) {
            gLog.warn("Error logging message", e);
        }

        return toString;
    }

    public int hashCode() {
        return mIdentifier.hashCode();
    }

    public boolean equals(Object o) {
        return getClass().equals(o.getClass()) && mIdentifier.equals(o);
    }
}
