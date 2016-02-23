package org.broadinstitute.gpinformatics.automation;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import jfxtras.labs.fxml.JFXtrasBuilderFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Wraps FXMLLoader to include spring resources defined in @JanusAppConfig
 */
public class SpringFxmlLoader {

    private static final AnnotationConfigApplicationContext applicationContext =
            new AnnotationConfigApplicationContext(JanusAppConfig.class);
    private FXMLLoader loader;

    public Object load(String url) {
        try (InputStream fxmlStream = SpringFxmlLoader.class
                .getResourceAsStream(url)) {
            System.err.println(SpringFxmlLoader.class
                    .getResourceAsStream(url));
            loader = new FXMLLoader();
            loader.setBuilderFactory(new JFXtrasBuilderFactory());
            loader.setLocation(this.getClass().getResource("CrspJanusApplication.fxml"));
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> clazz) {
                    return applicationContext.getBean(clazz);
                }
            });
            return loader.load(fxmlStream);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}