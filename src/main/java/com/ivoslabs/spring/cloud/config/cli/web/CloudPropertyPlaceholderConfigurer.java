package com.ivoslabs.spring.cloud.config.cli.web;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 *
 * <br>
 *
 * 1.- Implementing CloudPropertyPlaceholderConfigurer in spring
 *
 * <pre>
 * <code>&lt;bean id="propertyConfigurer" class="com.ivoslabs.spring.cloud.config.cl.web.CloudPropertyPlaceholderConfigurer">
	&lt;property name="environment" ref="environment" />
	&lt;property name="locations">
		&lt;list>
			&lt;value>classpath:project.properties&lt;/value>
		&lt;/list>
	&lt;/property>

&lt;/bean></code>
 * </pre>
 *
 * 2.- Add the properties required to connect to a spring-cloud-config-server service
 *
 * <pre>
 * <code>
 #######################
# spring cloud config #
#######################

spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.name=spring_cloud_conf_web_example
spring.cloud.config.profile=dev
spring.cloud.config.username=example-usr
spring.cloud.config.password=example-pwd</code>
 * </pre>
 *
 * @since 1.0.0
 * @author www.ivoslabs.com
 *
 */
public class CloudPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    /** The constant logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudPropertyPlaceholderConfigurer.class);

    /** spring-cloud-config URI */
    private String uri;

    /** spring-cloud-config Name */
    private String appName;

    /** spring-cloud-config Profile */
    private String profile;

    /** spring-cloud-config Username */
    private String username;

    /** spring-cloud-config Password */
    private String password;

    /** Interface representing the environment in which the current application is running */
    private Environment environment;

    /** The merged properties */
    private Properties mergedProperties;

    /** Bean factory used to resolve properties */
    private ConfigurableListableBeanFactory beanFactory;

    /*
     *
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        LOGGER.info("Loading the spring-cloud-config-server configuration");

        try {

            // save a reference to the beanfactory to resolve properties later
            this.beanFactory = beanFactory;

            Properties mergedProps = super.mergeProperties();

            // get from the local properties the spring-cloud configuration
            this.appName = mergedProps.getProperty("spring.cloud.config.name");
            this.profile = mergedProps.getProperty("spring.cloud.config.profile");
            this.uri = mergedProps.getProperty("spring.cloud.config.uri");
            // get the authentication data using the convertPropertyValue method
            // because this method can be overriden to implement a cipher
            this.username = this.convertPropertyValue(mergedProps.getProperty("spring.cloud.config.username"));
            this.password = this.convertPropertyValue(mergedProps.getProperty("spring.cloud.config.password"));

        } catch (IOException e) {
            throw new BeanInitializationException("There was an error loading the spring-cloud-config-server configuration", e);
        }

        super.postProcessBeanFactory(beanFactory);

    }

    /*
     *
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#convertPropertyValue(java.lang.String)
     */
    @Override
    protected String convertPropertyValue(String originalValue) {
        // this method can be overriden to implement a cipher
        return originalValue;
    }

    /*
     *
     * (non-Javadoc)
     *
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#mergeProperties()
     */
    @Override
    protected Properties mergeProperties() throws IOException {
        this.mergedProperties = super.mergeProperties();
        this.loadRemoteProperties();
        return this.mergedProperties;
    }

    /**
     * Resolves the given embedded value, e.g. an annotation attribute.
     *
     * @param key the value to resolve
     * @return the resolved value (may be the original value as-is)
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    String resolveCloudProperty(String key) {
        return this.beanFactory.resolveEmbeddedValue(key);
    }

    /**
     *
     * Load remote properties from a spring-cloud-config server service
     *
     * @since 1.0.0
     * @author www.ivoslabs.com
     */
    public void loadRemoteProperties() {

        LOGGER.info("Loading the remote properties from uri: {}; profile: {}", this.uri, this.profile);

        // spring-cloud-config-server configuration
        ConfigClientProperties configClientProperties = new ConfigClientProperties(this.environment);

        configClientProperties.setUri(new String[] { this.uri });
        configClientProperties.setName(this.appName);
        configClientProperties.setProfile(this.profile);
        configClientProperties.setUsername(this.username);
        configClientProperties.setPassword(this.password);
        configClientProperties.setFailFast(Boolean.TRUE);

        // in order to ConfigServicePropertySourceLocator use the correct config name
        // it is saved as system property
        ((StandardServletEnvironment) this.environment).getSystemProperties().put("spring.cloud.config.name", this.appName);

        // consume service to load remote properties
        PropertySource<?> propertySource = new ConfigServicePropertySourceLocator(configClientProperties).locate(this.environment);

        if (propertySource instanceof CompositePropertySource) {

            // cast to CompositePropertySource to use the getPropertyNames method
            CompositePropertySource remotePropertySource = (CompositePropertySource) propertySource;

            // add the remote properties in the merged properties
            if (remotePropertySource != null) {
                // property names of the remote properties
                String[] names = remotePropertySource.getPropertyNames();
                // search the value for each property and add them in the merged properties
                Stream.of(names).forEach(name -> this.mergedProperties.put(name, this.convertPropertyValue((String) remotePropertySource.getProperty(name))));
            }

            LOGGER.info("Remote properties were initialized. uri: {}; profile: {}", this.uri, this.profile);
        } else if (propertySource == null) {
            LOGGER.warn("The PropertySource as result of consume the remote properties is null");
        } else if (LOGGER.isWarnEnabled()) {
            String clzz = propertySource.getClass().getName();
            LOGGER.warn("The PropertySource as result of consume the remote properties is not a CompositePropertySource, the result was a {}", clzz);
        }

    }

    /**
     *
     * Sets the spring environment
     *
     * @param environment the spring environment
     * @since 1.0.0
     * @author www.ivoslabs.com
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
