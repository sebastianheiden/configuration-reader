package com.sheiden.configuraion.test.classes;

import java.math.BigDecimal;

import com.sheiden.configuration.annotation.ConfigurationProperty;
import com.sheiden.configuration.annotation.NameSpace;

@NameSpace("sub")
public class AdvancedSubConfiguration extends AdvancedSuperConfiguration {

	@ConfigurationProperty("string.name")
	public String b;

	public BigDecimal dec;

	public String c = "xyz";

}
