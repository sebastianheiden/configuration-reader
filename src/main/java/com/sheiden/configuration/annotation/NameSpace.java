package com.sheiden.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a name space for all fields in the annotated class. The defined name space will be treat as prefix of regarding property name.<br/>
 * Name spaces of inherited classes will be concatenated.
 * 
 * @author Sebastian Heiden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NameSpace {

	/**
	 * 
	 * @return the name space
	 */
	String value();

}
