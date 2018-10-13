package com.sheiden.configuraion.test.classes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sheiden.configuration.annotation.ConfigurationProperty;

public class CollectionConfiguration {

	public Map<String, MapSubConfiguration> map = new HashMap<>();

	@ConfigurationProperty("map1")
	public Map<Integer, Float> simpleMap = new HashMap<>();

	public List<String> list;

	public Set<Integer> set;

	public List<String> defaultList = Arrays.asList("a", "b");

}
