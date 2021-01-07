package de.gnox.rovy.server;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class StringUtil {
	
	public static String valueWithUnitToString(BigDecimal value, String unit) {
		if (value == null)
			return "";
		return df.format(value) + unit;
	}
	
	public static String valueWithUnitToString(Integer value, String unit) {
		if (value == null)
			return "";
		return value + unit;
	}
	
	
	public static String fillBefore(int length, char digit, String value) {
		if (value == null)
			return "";
		String result = value;
		while(result.length() < length)
			result = digit + result;
		return result;
	}
	
	private static DecimalFormat df = new DecimalFormat("0");
	
}
