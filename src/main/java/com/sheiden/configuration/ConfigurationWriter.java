package com.sheiden.configuration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 
 * This class helps to create JUnit tests (or other scenarios) with properties in interaction with
 * {@link ConfigurationReader}.<br/>
 * Given a test case, that evaluates a class, which is configured via properties: Instead of setting
 * all properties manually or creating a *.properties file for each test case, ConfigurationWriter
 * can be used to insert each property type-save and in the correct name space.
 * 
 * @author Sebastian Heiden
 */
public class ConfigurationWriter {

	private final Map<Class<?>, Function<?, String>> CLASS_MAPPERS = new HashMap<>();

	/**
	 * Instance for singleton usage
	 */
	private static ConfigurationWriter instance = null;

	/**
	 * Default Constructor. For a global instance use {@link #getInstance()} instead.<br/>
	 * This constructor should not been used, if custom class mappings are used to avoid registering the
	 * same mapping at all usages of ConfigurationWriter.
	 */
	public ConfigurationWriter() {

		// simple classes
		addClassMapper(String.class, str -> str);
		addClassMapper(Integer.class, i -> i.toString());
		addClassMapper(Long.class, l -> l.toString());
		addClassMapper(Float.class, f -> f.toString());
		addClassMapper(Double.class, d -> d.toString());
		addClassMapper(Boolean.class, b -> b.toString());

		// reverse arrays
		addClassMapper(String[].class, arr -> Arrays.asList(arr).stream().collect(Collectors.joining(",")));
		addClassMapper(Integer[].class, arr -> Arrays.asList(arr).stream().map(x -> x.toString()).collect(Collectors.joining(",")));
		addClassMapper(Long[].class, arr -> Arrays.asList(arr).stream().map(x -> x.toString()).collect(Collectors.joining(",")));
		addClassMapper(Float[].class, arr -> Arrays.asList(arr).stream().map(x -> x.toString()).collect(Collectors.joining(",")));
		addClassMapper(Double[].class, arr -> Arrays.asList(arr).stream().map(x -> x.toString()).collect(Collectors.joining(",")));
		addClassMapper(Boolean[].class, arr -> Arrays.asList(arr).stream().map(x -> x.toString()).collect(Collectors.joining(",")));

		// collections
		addClassMapper(List.class, list -> {

			@SuppressWarnings("unchecked")
			String result = (String) list.stream().map(x -> x.toString()).collect(Collectors.joining(","));
			return result;
		});

		addClassMapper(Set.class, set -> {

			@SuppressWarnings("unchecked")
			String result = (String) set.stream().map(x -> x.toString()).collect(Collectors.joining(","));
			return result;
		});
	}

	/**
	 * Method to get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static ConfigurationWriter getInstance() {
		if (instance == null) {
			instance = new ConfigurationWriter();
		}
		return instance;
	}

	/**
	 * Adds a new (custom) class mapping function. This mapping function is used to serialize the
	 * regarding object. If no mapping function is provided, {@link Object#toString() toString()} is
	 * called on the regarding object.
	 * 
	 * @param type the class that should be mapped
	 * @param func the function, that converts a String to M
	 */
	public <M> void addClassMapper(Class<M> type, Function<M, String> func) {
		CLASS_MAPPERS.put(type, func);
	}

	/**
	 * Writes each non <tt>null</tt> value of fields of given <tt>object</tt> as property to given
	 * <tt>properties</tt>.
	 * 
	 * @param properties properties object to write each property to
	 * @param object     object, that contains fields, which are written to <tt>properties</tt>
	 */
	public void write(Properties properties, Object object) {

		if (properties == null || object == null)
			return;

		Class<?> clazz = object.getClass();

		for (Field field : clazz.getFields()) {

			try {
				Object value = field.get(object);
				if (value == null)
					continue;

				String name = ConfigurationUtil.getPropertyName(field);

				Class<?> type = field.getType();

				if (CLASS_MAPPERS.containsKey(type)) {
					properties.setProperty(name, apply(value, type));

				} else if (type.equals(Map.class)) {
					handleMap(properties, name, value, clazz);

				} else {
					properties.setProperty(name, value.toString());
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.out.println("Can not write properties: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void handleMap(Properties properties, String name, Object value, Class<?> clazz) throws IllegalAccessException {

		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) value;
		for (Entry<Object, Object> entry : map.entrySet()) {

			Object entryKey = entry.getKey();
			Object entryValue = entry.getValue();

			if (!CLASS_MAPPERS.containsKey(entryKey.getClass()))
				throw new IllegalArgumentException("Key of map " + name + " in class " + clazz + " has unsupported type " + entryKey.getClass());

			String key = apply(entryKey, entryKey.getClass());

			if (CLASS_MAPPERS.containsKey(entryValue.getClass())) {

				String stringValue = apply(entryValue, entryValue.getClass());
				properties.setProperty(name + "." + key, stringValue);
			} else {

				// assume a complex object, if no class mapping is present
				for (Entry<String, String> subEntry : mapComplexObject(entryValue).entrySet()) {
					properties.setProperty(name + "." + key + "." + subEntry.getKey(), subEntry.getValue());
				}
			}
		}
	}

	private Map<String, String> mapComplexObject(Object object) throws IllegalAccessException {

		Map<String, String> map = new HashMap<>();

		for (Field field : object.getClass().getFields()) {

			String name = ConfigurationUtil.getPropertyName(field);
			Object value = field.get(object);
			Class<?> type = field.getType();

			map.put(name, CLASS_MAPPERS.containsKey(type) ? apply(value, type) : value.toString());
		}

		return map;
	}

	private String apply(Object object, Class<?> type) {
		@SuppressWarnings("unchecked")
		Function<Object, String> function = (Function<Object, String>) CLASS_MAPPERS.get(type);
		return function.apply(object);
	}

}
