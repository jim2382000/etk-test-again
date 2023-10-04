package net.micropact.aea.du.utility.mismatchedColumnTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.entellitrak.configuration.DataElement;

public class MismatchedColumnTypesUtil {

	private MismatchedColumnTypesUtil() {}

	public static long findNumberOfDecimalPlaces(final DataElement dataElement) {
		final String mask = dataElement.getMask();

    	final Matcher matcher = Pattern.compile("%,\\.(?<numberOfDigits>\\d+)f$").matcher(mask);
    	matcher.find();
    	final String numberOfDigitsAsString = matcher.group("numberOfDigits");
		return Long.parseLong(numberOfDigitsAsString);
	}
}
