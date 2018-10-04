package com.sheiden.configuration;

import java.lang.reflect.Field;

import com.sheiden.configuration.annotation.ConfigurationProperty;
import com.sheiden.configuration.annotation.NameSpace;

public class ConfigurationUtil {

	static String getPropertyName(Field field) {

		String nameSpace = ConfigurationUtil.getNameSpace(field.getDeclaringClass(), true);

		ConfigurationProperty propertyName = field.getAnnotation(ConfigurationProperty.class);
		if (propertyName == null)
			return nameSpace + field.getName();

		String name = propertyName.value();
		if (isEmpty(name))
			return nameSpace + field.getName();

		return nameSpace + name;
	}

	/**
	 * Recursive function to evaluate the name space of the given class.
	 * 
	 * @param clazz       the class to evaluate the name space
	 * @param actualClass flag to indicate the top level class
	 * @return the resulting name space for the given class
	 */
	private static String getNameSpace(Class<?> clazz, boolean actualClass) {

		String prefix = "";

		Class<?> superclass = clazz.getSuperclass();
		if (!superclass.equals(Object.class)) {
			prefix += getNameSpace(superclass, false);
		}

		NameSpace annotation = clazz.getAnnotation(NameSpace.class);
		if (annotation == null)
			return prefix;

		if (actualClass || annotation.inherit()) {
			if (annotation.override()) {
				prefix = annotation.value();
			} else {
				prefix += annotation.value();
			}
		}

		if (!prefix.isEmpty() && !prefix.endsWith(".")) {
			prefix += ".";
		}

		return prefix;
	}

	private static boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

}
