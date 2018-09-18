package com.sheiden.configuraion.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.sheiden.configuraion.test.classes.AdvancedConfiguration;
import com.sheiden.configuraion.test.classes.SimpleConfiguration;
import com.sheiden.configuration.ConfigurationReader;

public class ConfigurationReaderTest {

	private static final String PROPERTIES_BASE_DIR = "src/test/resources/properties/";

	/**
	 * Tests simple class mappings with basic file loader
	 */
	@Test
	public void simpleConfigReadTest() {

		SimpleConfiguration instance = new ConfigurationReader().read(PROPERTIES_BASE_DIR + "simple.properties", SimpleConfiguration.class);

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
	 * Tests advanced mechanics like name mapping, additional class mappings, default values, etc
	 */
	@Test
	public void advancedConfigReadTest() {

		ConfigurationReader configurationReader = new ConfigurationReader();
		configurationReader.addClassMapper(BigDecimal.class, str -> new BigDecimal(str));

		AdvancedConfiguration instance = configurationReader.read(PROPERTIES_BASE_DIR + "advanced.properties", AdvancedConfiguration.class);

		// tests naming per @PropertyName
		assertEquals("abc", instance.a);

		// tests additional class mapping -> BigDecimal
		assertEquals(new BigDecimal("1.23"), instance.dec);

		// tests default value
		assertEquals("xyz", instance.b);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingClassMapper() {
		new ConfigurationReader().read(PROPERTIES_BASE_DIR + "advanced.properties", AdvancedConfiguration.class);
	}

	@Test(expected = IllegalStateException.class)
	public void testFileIsDirectory() {
		new ConfigurationReader().read(PROPERTIES_BASE_DIR, AdvancedConfiguration.class);
	}

	@Test(expected = IllegalStateException.class)
	public void testFileIsMissing() {
		new ConfigurationReader().read(PROPERTIES_BASE_DIR + "does-not-exist", AdvancedConfiguration.class);
	}

}