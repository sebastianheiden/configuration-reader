package com.sheiden.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sheiden.configuration.ConfigurationReader;

/**
 * Specifies additional parameters of a configuration field.
 * 
 * @author Sebastian Heiden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationProperty {

	/**
	 * Defines the name of the property, that should be mapped to the annotated field.
	 * 
	 * @return the lookup property name of the field
	 */
	String value();

	/**
	 * Defines if the property is required. <br/>
	 * If the annotated property is required, but no value is provided, an IllegalArgumentException is
	 * thrown during {@link ConfigurationReader#read(java.util.Properties, Class)
	 * ConfigurationReader.read()}.
	 * 
	 * @return the requirement of the annotated field
	 */
	boolean required() default true;

	// TODO: methods min, max, pattern, etc...

}
