package com.ivoslabs.spring.cloud.config.cli.web;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

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
 * <code>   #
    # Remote properties conf
    #
    # enviroment
    spring.profiles.active=spring_cloud_conf_web_example
    spring.cloud.config.uri=http://localhost:8888
    spring.cloud.config.username=example-usr
    spring.cloud.config.password=example-pwd
    
    # activa cifrado
    security.placeHolder.enabled=true</code>
 * </pre>
 * 
 * @author imperezovan
 *
 */
public class CloudPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    /** The constant logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudPropertyPlaceholderConfigurer.class);

    /** servicio spring-cloud-config URI */
    private String uri;

    /** servicio spring-cloud-config Profile */
    private String profile;

    /** servicio spring-cloud-config Username */
    private String username;

    /** spring-cloud-config Password */
    private String password;

    /** Bean Environment */
    private Environment environment;

    /** Propeties */
    private Properties props;

    /*
     * 
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

	try {

	    LOGGER.info("Loading spring-cloud-config-server configuration");

	    Properties mergedProps = mergeProperties();
	    // from local properties it will load the accessa data to connect to a spring-cloud-config-server service
	    this.profile = mergedProps.getProperty("spring.profiles.active");
	    this.uri = mergedProps.getProperty("spring.cloud.config.uri");
	    this.username = super.convertPropertyValue(mergedProps.getProperty("spring.cloud.config.username"));
	    this.password = super.convertPropertyValue(mergedProps.getProperty("spring.cloud.config.password"));

	} catch (IOException ex) {
	    throw new BeanInitializationException("A error ocurred loading remote properties", ex);
	}

	super.postProcessBeanFactory(beanFactory);

    }

    /*
     * 
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#convertProperties(java.util.Properties)
     */
    @Override
    protected void convertProperties(Properties props) {
	this.props = props;

	this.loadRemoteProperties();

	super.convertProperties(props);
    }

    /**
     * Gets the value of a property by the key
     * 
     * @param key the kye
     * @return the found value
     * @author imperezivan
     *
     */
    String getProperty(String key) {
	return this.props.getProperty(key);
    }

    /**
     * 
     * Load properties from a spring-cloud-config server service
     * 
     * @author imperezivan
     *
     */
    void loadRemoteProperties() {

	LOGGER.info("Loading remote properties from uri: {}; profile: {}", this.uri, this.profile);

	PropertySource<?> properySource;

	// spring-cloud-config-server configuration
	ConfigClientProperties configClientProperties = new ConfigClientProperties(this.environment);
	configClientProperties.setUri(this.uri);
	configClientProperties.setName("application");
	configClientProperties.setProfile(this.profile);
	configClientProperties.setUsername(this.username);
	configClientProperties.setPassword(this.password);
	configClientProperties.setFailFast(Boolean.TRUE);

	// consume service to load properties
	properySource = new ConfigServicePropertySourceLocator(configClientProperties).locate(this.environment);

	if (properySource instanceof CompositePropertySource) {
	    // property names of configured at remote server
	    String[] names = ((CompositePropertySource) properySource).getPropertyNames();
	    // search the value for each propertie and add it to local properties instance

	    Stream.of(names).forEach(name -> this.props.setProperty(name, (String) properySource.getProperty(name)));
	}

	LOGGER.info("Remote properties   initialized uri: {}; profile: {}", this.uri, this.profile);
    }

    /**
     * Sets the environment
     *
     * @param environment {@code Environment} The environment to set
     */
    public void setEnvironment(Environment environment) {
	this.environment = environment;
    }

}
