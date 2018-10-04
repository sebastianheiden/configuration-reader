package com.sheiden.configuraion.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.sheiden.configuraion.test.classes.AdvancedSubConfiguration;
import com.sheiden.configuraion.test.classes.SimpleConfiguration;
import com.sheiden.configuration.ConfigurationReader;

public class ConfigurationReaderTest {

	private static final String PROPERTIES_BASE_DIR = "src/test/resources/properties/";

	/**
	 * Tests simple class mappings with basic file loader
	 */
	@Test
	public void simpleConfigReadTest() {

		SimpleConfiguration instance = ConfigurationReader.getInstance().read(PROPERTIES_BASE_DIR + "simple.properties", SimpleConfiguration.class);

		assertEquals("abc", instance._string);
		assertEquals(new Integer(1), instance._int);
		assertEquals(new Long(1), instance._long);
		assertEquals(new Float(1.23), instance._float);
		assertEquals(new Double(1.23), instance._double);
		assertEquals(new Boolean(true), instance._boolean);

		assertArrayEquals(new String[] { "a", "b", "c" }, instance._strings);
		assertArrayEquals(new Integer[] { 1, 2, 3 }, instance._ints);
		assertArrayEquals(new Long[] { 1l, 2l, 3l }, instance._longs);
		assertArrayEquals(new Float[] { 1.23f, 2.34f, 3.45f }, instance._floats);
		assertArrayEquals(new Double[] { 1.23d, 2.34d, 3.45d }, instance._doubles);
		assertArrayEquals(new Boolean[] { true, false, true }, instance._booleans);
	}

	/**
	 * Tests advanced mechanics like name mapping, additional class mappings, default values, name
	 * spaces, inheritance
	 */
	@Test
	public void advancedConfigReadTest() {

		ConfigurationReader configurationReader = ConfigurationReader.getInstance();
		configurationReader.addClassMapper(BigDecimal.class, str -> new BigDecimal(str));

		AdvancedSubConfiguration instance = configurationReader.read(PROPERTIES_BASE_DIR + "advanced.properties", AdvancedSubConfiguration.class);

		// tests inherited property with name space
		assertEquals("qwertz", instance.a);

		// tests naming per @ConfigurationProperty
		assertEquals("abc", instance.b);

		// tests additional class mapping -> BigDecimal
		assertEquals(new BigDecimal("1.23"), instance.dec);

		// tests default value
		assertEquals("xyz", instance.c);
	}

	/**
	 * Tests a missing class mapping for BigDecimal
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMissingClassMapper() {
		new ConfigurationReader().read(PROPERTIES_BASE_DIR + "advanced.properties", AdvancedSubConfiguration.class);
	}

	/**
	 * Tests that the given file path is not a file
	 */
	@Test(expected = IllegalStateException.class)
	public void testFileIsDirectory() {
		ConfigurationReader.getInstance().read(PROPERTIES_BASE_DIR, AdvancedSubConfiguration.class);
	}

	/**
	 * Tests that the given file path does not exist
	 */
	@Test(expected = IllegalStateException.class)
	public void testFileIsMissing() {
		ConfigurationReader.getInstance().read(PROPERTIES_BASE_DIR + "does-not-exist", AdvancedSubConfiguration.class);
	}

}
