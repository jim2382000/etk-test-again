package net.entellitrak.aea.gl.api.java;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for dealing with Strings.
 *
 * @author Zachary.Miller
 */
public final class StringUtil {

	/**
	 * Utility classes do not need public constructors.
	 */
	private StringUtil() {
	}

	/**
	 * Converts a string to a non-null representation.
	 * If the string is null, it will be converted to the empty string.
	 *
	 * @param string the string
	 * @return a non-null string
	 *
	 * @see #toNonEmptyString(String)
	 */
	public static String toNonNullString(final String string) {
		return Optional.ofNullable(string)
				.orElse("");
	}

	/**
	 * Convert a string to a non empty representation.
	 * If the string is empty, it will be converted to null.
	 *
	 * @param string the string
	 * @return the non-empty string
	 *
	 * @see #toNonNullString(String)
	 */
	public static String toNonEmptyString(final String string) {
		if(Objects.equals("", string)) {
			return null;
		} else {
			return string;
		}
	}

	/**
	 * Convert a string to unix-style line endings (\n).
	 *
	 * @param string the string
	 * @return the string with unix-style line endings
	 */
	public static String toUnixLineEndings(final String string) {
		return Optional.ofNullable(string)
				.map(str -> str.replace("\r\n", "\n"))
				.orElse(null);
	}
}
