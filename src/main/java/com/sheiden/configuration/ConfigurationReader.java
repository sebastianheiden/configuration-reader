package com.sheiden.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sheiden.configuration.annotation.ConfigurationProperty;

/**
 * This class reads property files and maps each property to a field of a given class.
 * 
 * @author Sebastian Heiden
 */
public class ConfigurationReader {

	private final Map<Type, Function<String, ?>> CLASS_MAPPERS = new HashMap<>();

	/**
	 * Instance for singleton usage
	 */
	private static ConfigurationReader instance = null;

	/**
	 * Default Constructor. For a global instance use {@link #getInstance()} instead.<br/>
	 * This constructor should not been used, if custom class mappings are used to avoid registering the
	 * same mapping at all usages of ConfigurationReader.
	 */
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

	/**
	 * Method to get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static ConfigurationReader getInstance() {
		if (instance == null) {
			instance = new ConfigurationReader();
		}
		return instance;
	}

	/**
	 * Adds a new (custom) class mapping function.
	 * 
	 * @param type the class that should be mapped
	 * @param func the function, that converts a String to M
	 */
	public <M> void addClassMapper(Class<M> type, Function<String, M> func) {
		CLASS_MAPPERS.put(type, func);
	}

	/**
	 * Tries to map each field of given <tt>configClass</tt> to a property from the properties file,
	 * located at <tt>pathToPropertiesFile</tt>.
	 * 
	 * @param                      <M> the type of the configuration class
	 * @param pathToPropertiesFile the path to a properties file on the file system
	 * @param configClass          the class object of the desired configuration object
	 * @return a new instance with the inserted fields of the provided class M
	 * @throws IllegalArgumentException If any required property is not set
	 * @throws IllegalStateException    If any field of the given class has an unsupported type
	 * 
	 * @see #read(Properties, Class)
	 */
	public <M> M read(String pathToPropertiesFile, Class<M> configClass) {
		return read(getProperties(pathToPropertiesFile), configClass);
	}

	/**
	 * Tries to map each field of given <tt>configClass</tt> to a property from <tt>properties</tt>.
	 * 
	 * @param             <M> the type of the configuration class
	 * @param properties  contains the properties, that are mapped to configClass
	 * @param configClass the class object of the desired configuration object
	 * @return a new instance with the inserted fields of the provided class M
	 * @throws IllegalArgumentException If any required property is not set
	 * @throws IllegalStateException    If any field of the given class has an unsupported type
	 * 
	 * @see #read(String, Class)
	 */
	public <M> M read(Properties properties, Class<M> configClass) {

		M instance = getInstance(configClass);
		if (properties == null) {
			System.out.println("Properties for class " + configClass.getName() + " is null");
			return instance;
		}

		for (Field field : configClass.getFields()) {

			checkField(field);

			Class<?> type = field.getType();
			boolean required = isRequired(field);

			try {

				// check collection classes
				if (type.equals(Map.class)) {
					handleMap(field, instance, properties);
					continue;
				} else if (type.equals(List.class)) {
					handleList(field, instance, properties);
					continue;
				} else if (type.equals(Set.class)) {
					handleSet(field, instance, properties);
					continue;
				}

				String propertyName = ConfigurationUtil.getPropertyName(field);
				String property = properties.getProperty(propertyName);

				if (property == null) {
					Object value = field.get(instance);
					if (required && value == null)
						throw new IllegalArgumentException("Property " + propertyName + " for class " + configClass + " is not set!");
				} else {

					if (!CLASS_MAPPERS.containsKey(type))
						throw new IllegalArgumentException( //
								String.format("Field %s in class %s has an unsupported type %s. Supported Types are: %s", //
										field.getName(), //
										configClass.getSimpleName(), //
										type.getSimpleName(), //
										accumulateSupportedTypes()));

					Object value = null;

					try {
						value = CLASS_MAPPERS.get(type).apply(property);
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalStateException("Unable to map property " + propertyName + " with value '" + property + "' to " + type.getSimpleName(), e);
					}

					field.set(instance, value);
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Can not instantiate config class: " + field.getName() + " is not accessable!");
			}
		}

		return instance;
	}

	private void checkField(Field field) {

		String name = field.getName();
		Class<?> declaringClass = field.getDeclaringClass();
		int modifiers = field.getModifiers();

		String baseError = String.format("Field %s in class %s ", name, declaringClass);

		if (!Modifier.isPublic(modifiers))
			throw new IllegalStateException(baseError + "must be public");

		if (Modifier.isStatic(modifiers))
			throw new IllegalStateException(baseError + "may not be static");

		if (Modifier.isFinal(modifiers))
			throw new IllegalStateException(baseError + "may not be final");

	}

	private Object read(Properties properties, Type configClass) {

		try {
			Class<?> clazz = Class.forName(configClass.getTypeName());
			return read(properties, clazz);

		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class of type " + configClass + " does not exist");
		}
	}

	private void handleSet(Field field, Object instance, Properties properties) throws IllegalAccessException {

		Optional<Stream<Object>> collection = handleCollection(field, instance, properties);

		if (collection.isPresent()) {
			field.set(instance, collection.get().collect(Collectors.toSet()));
		}
	}

	private void handleList(Field field, Object instance, Properties properties) throws IllegalAccessException {

		Optional<Stream<Object>> collection = handleCollection(field, instance, properties);

		if (collection.isPresent()) {
			field.set(instance, collection.get().collect(Collectors.toList()));
		}
	}

	private Optional<Stream<Object>> handleCollection(Field field, Object instance, Properties properties) throws IllegalAccessException {

		boolean required = isRequired(field);
		String propertyName = ConfigurationUtil.getPropertyName(field);

		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		Type[] actualTypeArguments = pt.getActualTypeArguments();

		// type and class mapping for the generic type
		Type keyType = actualTypeArguments[0];
		Function<String, ?> classMapper = CLASS_MAPPERS.get(keyType);

		String stringValue = properties.getProperty(propertyName);
		if (required && field.get(instance) == null && stringValue == null)
			throw new IllegalArgumentException("Property " + propertyName + " for class " + instance.getClass() + " is not set!");

		if (stringValue == null) {
			if (field.get(instance) == null)
				return Optional.of(Stream.empty());
			else
				return Optional.empty();
		}

		Stream<Object> stream = Arrays.asList(stringValue.split(",")).stream().map(s -> classMapper.apply(s));
		return Optional.of(stream);

	}

	private void handleMap(Field field, Object instance, Properties properties) throws IllegalAccessException {

		Set<String> keys = properties.keySet().stream().map(k -> k.toString()).collect(Collectors.toSet());

		boolean required = isRequired(field);
		String propertyName = ConfigurationUtil.getPropertyName(field);

		Set<String> matchingKeys = keys.stream().filter(k -> k.startsWith(propertyName + ".")).collect(Collectors.toSet());
		if (matchingKeys.isEmpty() && required && field.get(instance) == null)
			throw new IllegalArgumentException("Property " + propertyName + " for class " + instance.getClass() + " is not set!");

		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		Type[] actualTypeArguments = pt.getActualTypeArguments();

		// type and class mapping for the first generic type i.e. the key
		Type keyType = actualTypeArguments[0];
		Function<String, ?> keyMapper = CLASS_MAPPERS.get(keyType);

		// type and class mapping for the second generic type i.e. the value
		Type valueType = actualTypeArguments[1];
		Function<String, ?> valueMapper = CLASS_MAPPERS.get(valueType);

		if (keyMapper == null) {
			throw new IllegalArgumentException( //
					String.format("Field %s in class %s has an unsupported key type %s. Supported Types are: %s", //
							field.getName(), //
							instance.getClass().getSimpleName(), //
							valueType.toString(), //
							accumulateSupportedTypes()));
		}

		Map<Object, Object> map = new HashMap<>();

		// if no class mapping for the value is present, assume a complex object
		if (valueMapper == null) {

			Map<String, Properties> subProperties = createSubProperties(propertyName, matchingKeys, properties);
			Set<Entry<String, Properties>> entrySet = subProperties.entrySet();
			for (Entry<String, Properties> entry : entrySet) {

				Object key = keyMapper.apply(entry.getKey());
				Object value = read(entry.getValue(), valueType);

				map.put(key, value);
			}

		} else {

			for (String propertyKey : matchingKeys) {

				String stringKey = extractRelativeKey(propertyName, propertyKey);
				String stringValue = properties.getProperty(propertyKey);

				Object key = keyMapper.apply(stringKey);
				Object value = valueMapper.apply(stringValue);

				map.put(key, value);
			}
		}

		field.set(instance, map);
	}

	private Map<String, Properties> createSubProperties(String propertyName, Set<String> keys, Properties properties) {

		Map<String, List<String>> propertyParts = new HashMap<>();

		for (String propertyKey : keys) {

			String key = extractRelativeKey(propertyName, propertyKey);
			if (key == null || key.isEmpty())
				throw new IllegalArgumentException("Map " + propertyName + " needs a key");

			String[] split = key.split("\\.");

			List<String> parts = propertyParts.getOrDefault(split[0], new ArrayList<>());

			if (split.length > 1) {
				parts.add(split[1]);
			}

			propertyParts.putIfAbsent(split[0], parts);
		}

		Map<String, Properties> propertyList = new HashMap<>();

		for (Entry<String, List<String>> entry : propertyParts.entrySet()) {

			String subKey = entry.getKey();
			Properties subProperties = new Properties();

			for (String subSubKey : entry.getValue()) {
				subProperties.setProperty(subSubKey, properties.getProperty(propertyName + "." + subKey + "." + subSubKey));
			}

			propertyList.put(subKey, subProperties);
		}

		return propertyList;
	}

	private List<String> accumulateSupportedTypes() {

		Set<Type> types = new HashSet<>(CLASS_MAPPERS.keySet());
		types.add(Map.class);
		types.add(List.class);
		types.add(Set.class);

		return types.stream().map(t -> t.toString()).sorted().collect(Collectors.toList());
	}

	/**
	 * Extracts the relative path e.g. for parsing {@link Map} from properties.
	 * 
	 * @param prefix      the prefix, that is ignored
	 * @param propertyKey the full name of the property
	 * @return the relative key
	 */
	private String extractRelativeKey(String prefix, String propertyKey) {
		return propertyKey.substring(prefix.length() + 1);
	}

	/**
	 * Checks if the given field is necessary to set with a value from the properties.
	 * 
	 * @param field the field to check
	 * @see ConfigurationProperty#required()
	 * @return true, if the field must have a value, false else
	 */
	private boolean isRequired(Field field) {

		ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
		if (annotation == null)
			return true;

		return annotation.required();

	}

	/**
	 * Creates a new instance of given class.
	 * 
	 * @param       <M> the type of the given class
	 * @param clazz the given class to instantiate
	 * @return the new instance
	 * @throws IllegalArgumentException If the given class can not be instantiated, e.g. has no default
	 *                                  constructor
	 */
	private <M> M getInstance(Class<M> clazz) {

		try {
			return clazz.newInstance();

		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Can not instantiate config class: " + clazz.getName(), e);
		}
	}

	/**
	 * Reads the properties from the file, located at <tt>pathToPropertiesFile</tt>.
	 * 
	 * @param pathToPropertiesFile the path to a properties file
	 * @return a new properties object
	 * @throws IllegalStateException If the given path does not points to a valid, readable file
	 */
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
			if (configFile.isDirectory())
				throw new IllegalStateException("Properties file is a directory");
			else
				throw new IllegalStateException("Properties file does not exist");
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
