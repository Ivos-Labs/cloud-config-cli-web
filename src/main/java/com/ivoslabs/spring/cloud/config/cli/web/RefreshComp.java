package com.ivoslabs.spring.cloud.config.cli.web;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * Component to re-set the properties from a spring-cloud-config-server service
 * 
 * @since 1.0.0
 * @author www.ivoslabs.com
 *
 */
@Component
public class RefreshComp {

    /** The constant logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshComp.class);

    /** Configurer */
    @Autowired
    private CloudPropertyPlaceholderConfigurer configurer;

    /** Central interface to provide configuration for an application */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Reload the properties from a remote server and re set the new values into the singleton beans
     * 
     * @return json status as a json
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    public final String refresh() {
        String result;

        JsonObject status = new JsonObject();
        long ini = System.currentTimeMillis();

        try {
            // load properties
            this.configurer.loadRemoteProperties();
            // get current bean names
            String[] names = applicationContext.getBeanDefinitionNames();
            // valid if the bean is singleton
            Predicate<String> isSingleton = this.applicationContext::isSingleton;
            // re-asign the values
            Stream.of(names).filter(isSingleton).map(this.applicationContext::getBean).forEach(this::reloadFields);

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
     * Re set the values of attributes with {@code value} annotation
     * 
     * @param bean instance to reload
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    private void reloadFields(Object bean) {

        List<Field> fields = new ArrayList<>();

        this.getFields(bean.getClass(), fields);

        if (LOGGER.isInfoEnabled() && CollectionUtils.isNotEmpty(fields)) {
            LOGGER.info("Re-setting values in: {}", bean.getClass().getName());
        }

        fields.forEach(field -> this.setVal(bean, field));
    }

    /**
     * Get the fields with the {@code value} annotation
     * 
     * @param clazz  class where the fields will be extracted
     * @param fields array to save the found fields
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    private void getFields(Class<?> clazz, List<Field> fields) {

        // valid whether the field has the Value annotation
        Predicate<Field> hasValueAnnotation = field -> field.getDeclaredAnnotation(Value.class) != null;
        // add to fields the fields with the value annotation
        Stream.of(clazz.getDeclaredFields()).filter(hasValueAnnotation).forEach(fields::add);

        Class<?> superClass = clazz.getSuperclass();
        if (!superClass.equals(Object.class)) {
            // if the supper class isn't Object, read its fields with the Value annotation
            this.getFields(superClass, fields);
        }

    }

    /**
     * Re-assigns the value of an attribute to an instance
     * 
     * 
     * @param bean  instance where the value will be set
     * @param field field to re-set
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    private void setVal(Object bean, Field field) {

        field.setAccessible(Boolean.TRUE);

        try {

            String key = field.getDeclaredAnnotation(Value.class).value();
            String value = this.configurer.resolveCloudProperty(key);

            if (value != null) {
                this.parseNSetField(field, bean, value);
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Converts the value to the expected data type and assign it to the attribute
     * 
     * @param field field to set
     * @param bean  object where be set the value
     * @param value value to set
     * @throws NumberFormatException
     * @throws IllegalAccessException
     * @since 1.0.0
     * @author www.ivoslabs.com
     *
     */
    private void parseNSetField(Field field, Object bean, String value) throws IllegalAccessException {

        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            field.set(bean, Boolean.parseBoolean(value));
        } else if (field.getType() == int.class || field.getType() == Integer.class) {
            field.set(bean, Integer.parseInt(value));
        } else if (field.getType() == long.class || field.getType() == Long.class) {
            field.set(bean, Long.parseLong(value));
        } else if (field.getType() == float.class || field.getType() == Float.class) {
            field.set(bean, Float.parseFloat(value));
        } else if (field.getType() == double.class || field.getType() == Double.class) {
            field.set(bean, Double.parseDouble(value));
        } else if (field.getType() == BigDecimal.class) {
            field.set(bean, new BigDecimal(value));
        } else if (field.getType() == String.class) {
            field.set(bean, value);
        } else if (field.getType() == String[].class) {
            field.set(bean, Arrays.asList(value.split(",")).stream().map(String::trim).collect(Collectors.toList()).toArray(new String[] {}));
        } else if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("The Field  {}  in {} was ignored while loading properties process", field.getName(), bean.getClass().getName());
        }
    }

}
