package com.sheiden.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the property, that should be mapped to the annotated field.
 * 
 * @author sebastianheiden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

// TODO: rename to ConfigurationProperty with methods name, min, max, pattern, etc...

public @interface PropertyName {

	/**
	 * @return the lookup property name of the field
	 */
	String value();

}
