/**
 *
 */
package com.ivoslabs.spring.cloud.config.cli.web.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.ivoslabs.spring.cloud.config.cli.web.CloudPropertyPlaceholderConfigurer;
import com.ivoslabs.spring.cloud.config.cli.web.RefreshComp;

/**
 * Component to re-set the properties from a spring-cloud-config-server service
 *
 * @since 1.0.0
 * @author imperezivan
 *
 */
@Component @org.springframework.cloud.context.config.annotation.RefreshScope
public class RefreshCompImpl implements RefreshComp {

    /** The constant logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshCompImpl.class);

    /** Configurer */
    @Autowired
    private CloudPropertyPlaceholderConfigurer configurer;

    /** The applicationContext */
    @Autowired
    private ApplicationContext applicationContext;

    /** DefaultListableBeanFactory */
    @Autowired
    private DefaultListableBeanFactory beanFactory;

    /**
     * Creates a RefreshCompImpl instance
     */
    public RefreshCompImpl() {
        super();
    }

    /*
     *
     * (non-Javadoc)
     *
     * @see com.ivoslabs.spring.cloud.config.cli.web.RefreshComp#refresh()
     */
    @Override
    public final String refresh() {
        String result;

        JsonObject status = new JsonObject();
        long ini = System.currentTimeMillis();

        try {
            // reload properties
            this.configurer.loadRemoteProperties();
            LOGGER.info("Reloading properties");

            // se obtienen los nombres de los beans
            String[] names = applicationContext.getBeanDefinitionNames();
            // validate if is a sigleton bean
            Predicate<String> isSingleton = this.applicationContext::isSingleton;

            // validate if the field has the Autowire annotation
            Predicate<Field> hasAutowireAnnon = f -> f.getAnnotation(Autowired.class) != null;
            // validate if the field has the Value annotation
            Predicate<Field> hasValueAnnon = f -> f.getAnnotation(Value.class) != null;
            // validate if the field has the Autowire or Value annotation
            Predicate<Field> hasAnnotations = hasAutowireAnnon.or(hasValueAnnon);

            // filter the beans which has at least an attribute with the Value or Autowire annotation
            Predicate<String> hasValueOrAutoAtt = beanName -> this.getFields(beanName).stream().anyMatch(hasAnnotations);
            // destroy the bean
            Consumer<String> destroyBean = beanName -> this.beanFactory.destroySingleton(beanName);
            // log
            Consumer<String> print = beanName -> LOGGER.info("    Reloading bean {}", beanName);

            // filters beans to destroy
            List<String> beansToDestroy = Stream.of(names).filter(isSingleton).filter(hasValueOrAutoAtt).collect(Collectors.toList());
            // destroy beans
            beansToDestroy.stream().peek(print).forEach(destroyBean);

            LOGGER.info("Properties reloaded");

            status.addProperty("success", Boolean.TRUE);
        } catch (Exception e) {
            status.addProperty("success", Boolean.FALSE);
            status.addProperty("error", e.getMessage());
            if (e.getCause() != null) {
                status.addProperty("cause", e.getCause().getMessage());
            }
            LOGGER.error(e.getMessage(), e);
        }

        status.addProperty("time", (System.currentTimeMillis() - ini) + " ms");
        status.addProperty("date", LocalDateTime.now().toString());

        result = status.toString();

        return result;
    }

    /***
     * *
     ***/

    /***
     * *
     ***/

    /***
     * *
     ***/

    /*******************
     * Private methods *
     *******************/

    /**
     * Returns a list of Fields of the given beanName
     *
     * @param beanName name of the bean to use to extract fields
     * @return the list found fields
     * @since 1.0.0
     * @author imperezivan
     *
     */
    private List<Field> getFields(String beanName) {

        Class<?> clazz = this.applicationContext.getBean(beanName).getClass();

        List<Field> fields = new ArrayList<>();

        this.getFields(clazz, fields);

        return fields;
    }

    /**
     * Adds to the given list the found fields in the given class
     *
     * @param clazz  class to use to extract fields
     * @param fields list where will be added the found fields
     * @since 1.0.0
     * @author imperezivan
     *
     */
    private void getFields(Class<?> clazz, List<Field> fields) {

        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (!clazz.getSuperclass().equals(Object.class)) {
            this.getFields(clazz.getSuperclass(), fields);
        }

    }

}
