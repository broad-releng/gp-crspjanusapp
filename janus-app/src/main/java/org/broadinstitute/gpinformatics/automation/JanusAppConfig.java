package org.broadinstitute.gpinformatics.automation;

import com.google.common.io.Files;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.broadinstitute.gpinformatics.automation.controllers.FingerprintNormalizationController;
import org.broadinstitute.gpinformatics.automation.controllers.JanusApplicationController;
import org.broadinstitute.gpinformatics.automation.controllers.ProtocolsController;
import org.broadinstitute.gpinformatics.automation.controllers.LoginController;
import org.broadinstitute.gpinformatics.automation.controllers.NormalizationController;
import org.broadinstitute.gpinformatics.automation.controllers.RNACaliperQCController;
import org.broadinstitute.gpinformatics.automation.controllers.RaiseVolumeController;
import org.broadinstitute.gpinformatics.automation.control.ReagentForm;
import org.broadinstitute.gpinformatics.automation.controllers.ShearingNormalizationController;
import org.broadinstitute.gpinformatics.automation.controllers.SonicDaughterPlateCreationController;
import org.broadinstitute.gpinformatics.automation.controllers.TruSeqAliquotController;
import org.broadinstitute.gpinformatics.automation.controllers.TruSeqSpikeController;
import org.broadinstitute.gpinformatics.automation.controllers.VolumeTransferController;
import org.broadinstitute.gpinformatics.automation.messaging.BettaLIMSMessageSender;
import org.broadinstitute.gpinformatics.automation.messaging.HttpMessageTransport;
import org.broadinstitute.gpinformatics.automation.messaging.JmsMessageTransport;
import org.broadinstitute.gpinformatics.automation.model.User;
import org.broadinstitute.gpinformatics.automation.util.BadgeScanner;
import org.broadinstitute.gpinformatics.automation.util.MachineNameLookup;
import org.broadinstitute.gpinformatics.automation.util.RackScanner;
import org.broadinstitute.gpinformatics.automation.util.RootConfig;
import org.broadinstitute.gpinformatics.automation.util.RuntimeExecutor;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.broadinstitute.techdev.lims.mercury.LimsServiceMercuryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import java.io.File;

/**
 * Spring bindings configuration
 */
@Configuration
@PropertySource("/janus.app.properties")
public class JanusAppConfig {
    @Inject
    public Environment environment;

    @Bean
    public ProtocolsController janusAppController(){
        return new ProtocolsController();
    }

    @Bean
    public SonicDaughterPlateCreationController sonicDaughterPlateCreationController() {
        return new SonicDaughterPlateCreationController();
    }

    @Bean
    public VolumeTransferController volumeTransferController(){
        return new VolumeTransferController();
    }

    @Bean
    public FingerprintNormalizationController fingerprintNormalizationController(){
        return new FingerprintNormalizationController();
    }

    @Bean
    public ShearingNormalizationController shearingNormalizationController(){
        return new ShearingNormalizationController();
    }

    @Bean
    public NormalizationController normalizationController(){
        return new NormalizationController();
    }

    @Bean
    public RaiseVolumeController raiseVolumeController(){
        return new RaiseVolumeController();
    }

    @Bean
    public TruSeqAliquotController truSeqAliquotController(){
        return new TruSeqAliquotController();
    }

    @Bean
    public TruSeqSpikeController truSeqSpikeController(){
        return new TruSeqSpikeController();
    }

    @Bean
    public RNACaliperQCController rnaCaliperQCController(){
        return new RNACaliperQCController();
    }

    @Bean
    public LoginController loginController() {
        return new LoginController();
    }

    @Bean
    public JanusApplicationController janusApplicationController() {
        return new JanusApplicationController();
    }

    @Bean
    public ReagentForm reagentFormController() {
        return new ReagentForm();
    }

    @Bean
    public RootConfig rootConfig() {
        if(App.UI_TEST)
            return new RootConfig(Files.createTempDir());
        else
            return new RootConfig(getRootDir());
    }

    @Bean
    public LimsService limsService() {
        if(App.UI_TEST) {
            return new LimsServiceMercuryImpl(App.limsHostname);
        }
        else
            return new LimsServiceMercuryImpl(getHostname());
    }

    @Bean
    public User user() {
        return new User();
    }

    @Bean
    public MachineNameLookup machineNameLookup() {
        return new MachineNameLookup();
    }

    @Bean
    public BettaLIMSMessageSender messageSender(){
        JmsMessageTransport jmsMessageTransport = new JmsMessageTransport();

        ActiveMQQueue activeMQQueue = new ActiveMQQueue(getQueueName());
        JmsTemplate jmsTemplate = new JmsTemplate();

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        String serverName = getJmsServer();
        String serverPort = getJmsServerPort();
        String brokerUrl = String.format("tcp://%s:%s?connectionTimeout=60000", serverName, serverPort);
        connectionFactory.setBrokerURL(brokerUrl);

        jmsTemplate.setDefaultDestination(activeMQQueue);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setSessionTransacted(true);

        jmsMessageTransport.setJmsTemplate(jmsTemplate);

        HttpMessageTransport httpMessageTransport = getHttpMessageTransport();
        httpMessageTransport.setSuccessor(jmsMessageTransport);

        BettaLIMSMessageSender sender = new BettaLIMSMessageSender();
        sender.setMessageTransport(httpMessageTransport);
        sender.setStashDir(rootConfig().getStashDir());
        sender.setArchiveDir(rootConfig().getMessageArchiveDir());
        sender.setArchiveSentMessages(true);
        return sender;
    }

    @Bean
    public BadgeScanner getBadgeScanner() {
        return new BadgeScanner(new RuntimeExecutor(1000 * 60 * 2), getBadgeScannerFile());
    }

    @Bean
    public File getBadgeScannerFile() {
        return new File(getBadgeScannerExePath());
    }

    @Bean
    public HttpMessageTransport getHttpMessageTransport() {
        HttpMessageTransport messageTransport = new HttpMessageTransport();
        if(App.UI_TEST) {
            String url = String.format("http://%s/Mercury/rest/bettalimsmessage", App.limsHostname);
            messageTransport.setUrl(url);
        } else
            messageTransport.setUrl(getBettalimsMessageUrl());
        return messageTransport;
    }

    @Bean
    public String getAppVersion() {
        return environment.getProperty("app.version");
    }

    private String getJmsServerPort() {
        return environment.getProperty("jms.server.port");
    }

    private String getJmsServer() {
        return environment.getProperty("jms.server.name");
    }

    private String getQueueName() {
        return environment.getProperty("jms.queue.name");
    }

    private String getHostname() {
        return environment.getProperty("mercury.service.hostname");
    }

    private String getRootDir() {
        return environment.getProperty("rootconfig.dir");
    }

    private String getBettalimsMessageUrl() {
        return environment.getProperty("bettalims.http.url");
    }

    private String getBadgeScannerExePath() {
        return environment.getProperty("badgescanner.exe.path");
    }
}
