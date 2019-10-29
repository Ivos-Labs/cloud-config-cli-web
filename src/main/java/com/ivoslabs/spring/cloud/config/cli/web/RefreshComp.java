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
 * Component to reload a re-set the properties from a spring-cloud-config-server service
 * 
 * @author imperezivan
 *
 */
@Component
public class RefreshComp {

    /** The constant logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshComp.class);

    /** Configurer */
    @Autowired
    private CloudPropertyPlaceholderConfigurer configurer;

    /** The applicationContext */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Reload the properties from a remote server and re set the new values into the singleton beans
     * 
     * @return json status as a json
     * @author imperezivan
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
	    // re set the values
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

    /**
     * Re set the values of attributes with {@code value} annotation
     * 
     * @param bean instance to reload
     * @author imperezivan
     *
     */
    private void reloadFields(Object bean) {
	List<Field> fields = this.getFields(bean.getClass());

	if (LOGGER.isInfoEnabled() && CollectionUtils.isNotEmpty(fields)) {
	    LOGGER.info("Reasignando valores en: {}", bean.getClass().getName());
	}

	fields.forEach(field -> this.setVal(bean, field));
    }

    /**
     * Get the fiels with {@code value} annotation
     * 
     * @param clazz clas where will be extracted the fields
     * @return the list of found attributes
     * @author imperezivan
     *
     */
    private List<Field> getFields(Class<?> clazz) {
	List<Field> fields = new ArrayList<>();

	//
	Predicate<Field> hasValueAnnotation = field -> field.getDeclaredAnnotation(Value.class) != null;
	//
	fields.addAll(Stream.of(clazz.getDeclaredFields()).filter(hasValueAnnotation).collect(Collectors.toList()));

	Class<?> superClass = clazz.getSuperclass();
	//
	if (!superClass.equals(Object.class)) {
	    fields.addAll(this.getFields(superClass));
	}

	return fields;
    }

    /**
     * Re assigns the value of an attribute to an instance
     * 
     * 
     * @param bean  instance where will be set the value
     * @param field field to re-set
     * @author imperezivan
     *
     */
    private void setVal(Object bean, Field field) {

	field.setAccessible(Boolean.TRUE);

	try {
	    String key = field.getDeclaredAnnotation(Value.class).value();

	    key = key.substring(key.indexOf('{') + 1, key.indexOf(':') != -1 ? key.indexOf(':') : key.indexOf('}'));

	    String v = this.configurer.getProperty(key);

	    if (v != null) {
		this.parseNSetField(field, bean, v);
	    }

	} catch (IllegalArgumentException | IllegalAccessException e) {
	    LOGGER.error(e.getMessage(), e);
	}
    }

    /**
     * Convert the value to the expected data type and assign it to the attribute
     * 
     * @param field field to set
     * @param bean  object where be set the value
     * @param v     value to set
     * @throws NumberFormatException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @author imperezivan
     *
     */
    private void parseNSetField(Field field, Object bean, String v) throws IllegalAccessException {
	if (field.getType() == boolean.class || field.getType() == Boolean.class) {
	    field.set(bean, Boolean.parseBoolean(v));
	} else if (field.getType() == int.class || field.getType() == Integer.class) {
	    field.set(bean, Integer.parseInt(v));
	} else if (field.getType() == long.class || field.getType() == Long.class) {
	    field.set(bean, Long.parseLong(v));
	} else if (field.getType() == float.class || field.getType() == Float.class) {
	    field.set(bean, Float.parseFloat(v));
	} else if (field.getType() == double.class || field.getType() == Double.class) {
	    field.set(bean, Double.parseDouble(v));
	} else if (field.getType() == BigDecimal.class) {
	    field.set(bean, new BigDecimal(v));
	} else if (field.getType() == String.class) {
	    field.set(bean, v);
	} else if (field.getType() == String[].class) {
	    field.set(bean, Arrays.asList(v.split(",")).stream().map(String::trim).collect(Collectors.toList()).toArray(new String[] {}));
	} else if (LOGGER.isWarnEnabled()) {
	    LOGGER.warn("The Field  {}  in {} was ignored while loading properties process", field.getName(), bean.getClass().getName());
	}
    }

}
