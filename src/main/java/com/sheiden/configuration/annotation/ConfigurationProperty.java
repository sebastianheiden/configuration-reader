package com.sheiden.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

	// TODO: methods min, max, pattern, etc...

}
