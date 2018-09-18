package com.sheiden.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sheiden.configuration.annotation.PropertyName;

public class ConfigurationReader {

	private final Map<Class<?>, Function<String, ?>> CLASS_MAPPERS = new HashMap<>();

	public ConfigurationReader() {

		// single classes
		addClassMapper(String.class, str -> str);
		addClassMapper(Integer.class, str -> Integer.parseInt(str));
		addClassMapper(Long.class, str -> Long.parseLong(str));
		addClassMapper(Float.class, str -> Float.parseFloat(str));
		addClassMapper(Double.class, str -> Double.parseDouble(str));
		addClassMapper(Boolean.class, str -> Boolean.valueOf(str));

		// arrays
		addClassMapper(String[].class, str -> str.split(","));
		addClassMapper(Integer[].class, str -> Arrays.asList(str.split(",")).stream().map(s -> CLASS_MAPPERS.get(Integer.class).apply(s)).toArray(Integer[]::new));
		addClassMapper(Long[].class, str -> Arrays.asList(str.split(",")).stream().map(s -> CLASS_MAPPERS.get(Long.class).apply(s)).toArray(Long[]::new));
		addClassMapper(Float[].class, str -> Arrays.asList(str.split(",")).stream().map(s -> CLASS_MAPPERS.get(Float.class).apply(s)).toArray(Float[]::new));
		addClassMapper(Double[].class, str -> Arrays.asList(str.split(",")).stream().map(s -> CLASS_MAPPERS.get(Double.class).apply(s)).toArray(Double[]::new));
		addClassMapper(Boolean[].class, str -> Arrays.asList(str.split(",")).stream().map(s -> CLASS_MAPPERS.get(Boolean.class).apply(s)).toArray(Boolean[]::new));
	}

	public <M> void addClassMapper(Class<M> type, Function<String, M> func) {
		CLASS_MAPPERS.put(type, func);
	}

	public <M> M read(String pathToPropertiesFile, Class<M> configClass) {
		return read(getProperties(pathToPropertiesFile), configClass);
	}

	public <M> M read(Properties properties, Class<M> configClass) {

		M instance = getInstance(configClass);
		if (properties == null) {
			System.out.println("Properties for class " + configClass.getName() + " is null");
			return instance;
		}

		for (Field field : configClass.getDeclaredFields()) {

			Class<?> type = field.getType();
			if (!CLASS_MAPPERS.containsKey(type)) throw new IllegalArgumentException("Field " + field.getName() + " in class " + configClass + " has an unsupported type. Supported Types are: "
					+ CLASS_MAPPERS.keySet().stream().map(c -> c.getSimpleName()).sorted().collect(Collectors.toList()));

			String propertyName = getPropertyName(field);
			String property = properties.getProperty(propertyName);

			try {

				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				if (isEmpty(property)) {
					Object value = field.get(instance);
					if (value == null) throw new IllegalArgumentException("Property " + propertyName + " for class " + configClass + " is not set!");
				} else {

					try {
						field.set(instance, CLASS_MAPPERS.get(type).apply(property));
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalStateException("Unable to map property " + propertyName + " with value '" + property + "' to " + type.getSimpleName(), e);
					}
				}

			} catch (IllegalAccessException e) {
				System.out.println("Can not instanciate config class: " + e.getMessage());
				e.printStackTrace();
				throw new IllegalArgumentException("Can not instantiate config class: " + field.getName() + " is not accessable!");
			}
		}

		return instance;
	}

	private String getPropertyName(Field field) {

		PropertyName propertyName = field.getAnnotation(PropertyName.class);
		if (propertyName == null) return field.getName();

		String name = propertyName.value();
		if (isEmpty(name)) return field.getName();

		return name;
	}

	private boolean isEmpty(String name) {
		return name == null || name.isEmpty();
	}

	private <M> M getInstance(Class<M> clazz) {

		try {
			return clazz.newInstance();

		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Can not instantiate config class: " + clazz.getName());
		}
	}

	public Properties getProperties(String pathToPropertiesFile) {

		pathToPropertiesFile = pathToPropertiesFile.replaceFirst("^~", System.getProperty("user.home"));

		File configFile = new File(pathToPropertiesFile);

		try {
			System.out.println("Loading properties from: " + configFile.getCanonicalPath());
		} catch (IOException e) {
			System.out.println("Unable to determine properties file path");
			e.printStackTrace();
		}

		// Checks if file exists
		if (!configFile.isFile()) {
			if (configFile.isDirectory()) throw new IllegalStateException("Properties file is a directory");
			else throw new IllegalStateException("Properties file does not exist");
		}

		// Reads the properties from the file
		try (InputStream input = new FileInputStream(configFile)) {

			Properties shardProperties = new Properties();
			shardProperties.load(input);

			return shardProperties;
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read properties file", e);
		}
	}
}
