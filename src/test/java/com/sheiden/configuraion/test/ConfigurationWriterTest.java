
package com.sheiden.configuraion.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.sheiden.configuraion.test.classes.AdvancedSubConfiguration;
import com.sheiden.configuraion.test.classes.CollectionConfiguration;
import com.sheiden.configuraion.test.classes.MapSubConfiguration;
import com.sheiden.configuraion.test.classes.SimpleConfiguration;
import com.sheiden.configuration.ConfigurationReader;
import com.sheiden.configuration.ConfigurationWriter;

public class ConfigurationWriterTest {

	@Test
	public void testSimple() {

		SimpleConfiguration instance1 = new SimpleConfiguration();
		instance1._string = "abc";
		instance1._int = 1;
		instance1._long = 1L;
		instance1._float = 1.23F;
		instance1._double = 1.23D;
		instance1._boolean = true;

		instance1._strings = new String[] { "a", "b", "c" };
		instance1._ints = new Integer[] { 1, 2, 3 };
		instance1._longs = new Long[] { 1l, 2l, 3l };
		instance1._floats = new Float[] { 1.23f, 2.34f, 3.45f };
		instance1._doubles = new Double[] { 1.23d, 2.34d, 3.45d };
		instance1._booleans = new Boolean[] { true, false, true };

		Properties properties = new Properties();

		ConfigurationWriter.getInstance().write(properties, instance1);
		SimpleConfiguration instance2 = ConfigurationReader.getInstance().read(properties, SimpleConfiguration.class);

		assertEquals(instance1._string, instance2._string);
		assertEquals(instance1._int, instance2._int);
		assertEquals(instance1._long, instance2._long);
		assertEquals(instance1._float, instance2._float);
		assertEquals(instance1._double, instance2._double);
		assertEquals(instance1._boolean, instance2._boolean);

		assertArrayEquals(instance1._strings, instance2._strings);
		assertArrayEquals(instance1._ints, instance2._ints);
		assertArrayEquals(instance1._longs, instance2._longs);
		assertArrayEquals(instance1._floats, instance2._floats);
		assertArrayEquals(instance1._doubles, instance2._doubles);
		assertArrayEquals(instance1._booleans, instance2._booleans);

	}

	@Test
	public void testAdvanced() {

		AdvancedSubConfiguration subConfig = new AdvancedSubConfiguration();
		subConfig.a = "a";
		subConfig.b = "b";
		subConfig.c = "c";
		subConfig.dec = new BigDecimal("123.123");

		Properties properties = new Properties();
		ConfigurationWriter.getInstance().write(properties, subConfig);

		ConfigurationReader configurationReader = new ConfigurationReader();
		configurationReader.addClassMapper(BigDecimal.class, str -> new BigDecimal(str));
		AdvancedSubConfiguration subConfig2 = configurationReader.read(properties, AdvancedSubConfiguration.class);

		assertEquals(subConfig.a, subConfig2.a);
		assertEquals(subConfig.b, subConfig2.b);
		assertEquals(subConfig.c, subConfig2.c);
		assertEquals(subConfig.dec, subConfig2.dec);

	}

	@Test
	public void testCollections() {

		CollectionConfiguration collections = new CollectionConfiguration();
		collections.list = Arrays.asList("abc", "def", "ghi", "ghi");
		collections.set = new HashSet<>(Arrays.asList(1, 2, 3, 4));

		Map<String, MapSubConfiguration> map = new HashMap<>();
		map.put("a", new MapSubConfiguration("a", "b"));
		collections.map = map;

		Map<Integer, Float> simpleMap = new HashMap<>();
		simpleMap.put(1, 1F);
		collections.simpleMap = simpleMap;

		Properties properties = new Properties();
		ConfigurationWriter.getInstance().write(properties, collections);

		System.out.println(properties);

		CollectionConfiguration collections2 = ConfigurationReader.getInstance().read(properties, CollectionConfiguration.class);

		assertEquals(collections.list, collections2.list);
		assertEquals(collections.set, collections2.set);
		assertEquals(collections.map, collections2.map);
		assertEquals(collections.simpleMap, collections2.simpleMap);

	}

}
