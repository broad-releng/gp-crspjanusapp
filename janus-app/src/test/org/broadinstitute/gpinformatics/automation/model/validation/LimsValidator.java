package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.JanusAppConfig;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * When using spring injected lims service
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = JanusAppConfig.class)
public abstract class LimsValidator {
    @Autowired
    public LimsService limsService;
}
