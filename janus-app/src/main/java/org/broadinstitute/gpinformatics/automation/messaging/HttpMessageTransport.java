package org.broadinstitute.gpinformatics.automation.messaging;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import sun.security.util.HostnameChecker;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.ws.rs.core.MediaType;

/**
 * Send bettalims message through http protocol
 */
public class HttpMessageTransport implements MessageTransport {
    private static final Logger gLog = LoggerFactory.getLogger(HttpMessageTransport.class);
    public static final int MESSAGE_PERSISTED_STATUS = 200;
    private String url;
    private ClientResponse response;
    private MessageTransport successor;

    public HttpMessageTransport() {
    }

    public HttpMessageTransport(String url) {
        this.url = url;
    }

    public boolean sendMessage(Message message) {
        try{
            ClientConfig config = new DefaultClientConfig();
            acceptAllServerCertificates(config);
            Client client = Client.create(config);
            WebResource service = client.resource(url);
            response = service.type(MediaType.APPLICATION_XML).entity(message.messageToString()).post(ClientResponse.class);
            return response.getStatus() == MESSAGE_PERSISTED_STATUS || response.getStatus() == 201;
        } catch (Exception e){
            gLog.warn("HttpMessageTransport: error sending message to: " + url,e);
        }

        return successor != null && successor.sendMessage(message);
    }

    protected void acceptAllServerCertificates(ClientConfig config) {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());


            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }, sc
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ClientResponse getResponse() {
        return response;
    }

    public int getResponseStatus(){
        return response.getStatus();
    }

    public String getResponseText(){
        return response.getEntity(String.class);
    }

    @Required
    public void setUrl(String url) {
        this.url = url;
    }

    public void setSuccessor(MessageTransport successor) {
        this.successor = successor;
    }
}
