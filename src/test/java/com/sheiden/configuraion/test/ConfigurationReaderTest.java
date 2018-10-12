package com.sheiden.configuraion.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sheiden.configuraion.test.classes.AdvancedSubConfiguration;
import com.sheiden.configuraion.test.classes.CollectionConfiguration;
import com.sheiden.configuraion.test.classes.MapSubConfiguration;
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

	@Test
	public void testCollections() {

		CollectionConfiguration collections = ConfigurationReader.getInstance().read(PROPERTIES_BASE_DIR + "collection.properties", CollectionConfiguration.class);

		Map<String, MapSubConfiguration> map = collections.map;

		assertNotNull(map);
		assertEquals(3, map.size());
		assertEquals("1a", map.get("1").a);
		assertEquals("1b", map.get("1").b);
		assertEquals("2a", map.get("2").a);
		assertEquals("2b", map.get("2").b);
		assertEquals("empty", map.get("3").a);
		assertEquals("empty", map.get("3").b);

		Map<Integer, Float> map1 = collections.simpleMap;

		assertNotNull(map1);
		assertEquals(3, map1.size());
		assertEquals(new Float(.1F), map1.get(1));
		assertEquals(new Float(.2F), map1.get(2));
		assertEquals(new Float(.3F), map1.get(3));

		List<String> list = collections.list;

		assertNotNull(list);
		assertEquals(4, list.size());
		assertEquals(Arrays.asList("abc", "def", "ghi", "ghi"), list);

		Set<Integer> set = collections.set;

		assertNotNull(set);
		assertEquals(4, set.size());
		assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 3, 4)), set);
	}

}
