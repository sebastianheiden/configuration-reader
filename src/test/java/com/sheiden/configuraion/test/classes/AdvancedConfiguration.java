package com.sheiden.configuraion.test.classes;

import java.math.BigDecimal;

import com.sheiden.configuration.annotation.PropertyName;

public class AdvancedConfiguration {

	@PropertyName("advanced.string.name")
	public String a;

	public BigDecimal dec;

	public String b = "xyz";

}
