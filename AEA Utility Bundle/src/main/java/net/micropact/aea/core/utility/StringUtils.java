package net.micropact.aea.core.utility;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.micropact.aea.core.wrappedAPIs.StringUtilsWrapper;
import net.micropact.aea.utility.Utility;

/**
 * This class contains useful functions related to the {@link String} class.
 *
 * @author Zachary.Miller
 */
public final class StringUtils {

    /**
     * A {@link Pattern} representing both windows and unix line endings.
     */
    private static final Pattern NEWLINE_REGEX_PATTERN = Pattern.compile("\r?\n");

    /**
     * Utility classes do not need public constructors.
     */
    private StringUtils(){}

    /** Joins a list of Strings with a given separator.
     *
     * @param strings Strings to join
     * @param separator item to interpose between the strings
     * @return the separator-separated string
     */
    public static String join(final List<String> strings, final String separator) {
        String returnString;

        if(strings.isEmpty()){
            returnString = "";
        }else{
            final StringBuilder builder = new StringBuilder();
            for(final String string : strings){
                builder.append(separator).append(string);
            }
            returnString = builder.substring(separator.length());
        }
        return returnString;
    }

    /**
     * This method replaces all occurrences of searchString with replacementString within text.
     *
     * @param text the text to do replacements upon
     * @param searchString The text which should be replaced
     * @param replacementString the replacement for searchString
     * @return the replaced string
     */
    public static String replace(final String text, final String searchString, final String replacementString) {
        return StringUtilsWrapper.replace(text, searchString, replacementString);
    }

    /**
     * Checks that a String only contains unicode digits. This means that decimal points, minus signs, etc will
     * cause it to return false.
     *
     * @param string the string to check
     * @return whether the string contains only digits
     */
    public static boolean isNumeric(final String string) {
        return StringUtilsWrapper.isNumeric(string);
    }

    /**
     * Generates a String which is formed by repeating a particular string a number of times.
     *
     * @param string The string to repeat
     * @param times The times to repeat
     * @return the generated String
     */
    public static String repeat(final String string, final int times) {
        return StringUtilsWrapper.repeat(string, times);
    }

    /**
     * Checks whether a string is contained within another. Does not error when passed null values.
     *
     * @param seq The String which may contain the other
     * @param searchSeq The String which may be contained within the other
     * @return If searchSeq is found within seq.
     */
    public static boolean contains(final String seq, final String searchSeq) {
        return StringUtilsWrapper.contains(seq, searchSeq);
    }

    /**
     * Convert a possibly null string to a non-null string.
     * Converts null to the empty string.
     *
     * @param string the string
     * @return a non-null string
     *
     * @see #toStringNonEmpty(String)
     */
    public static String toStringNonNull(final String string) {
        return Utility.nvl(string, "");
    }

    /**
     * Convert a possibly empty string to a non-empty string.
     * Converts empty string to null.
     *
     * @param string the string
     * @return a possibly-null, non-empty string
     *
     * @see #toStringNonNull(String)
     */
    public static String toStringNonEmpty(final String string) {
        return Objects.equals("", string) ? null : string;
    }

    /**
     * Get the lines within a string.
     * The line separator may be either \n or \r\n.
     *
     * @param string the string
     * @return the lines
     */
    public static Stream<String> lines(final String string){
        return NEWLINE_REGEX_PATTERN.splitAsStream(string);
    }

    /**
     * Capitalize the first letter of a string.
     *
     * @param string the string
     * @return the string with the first letter capitalized
     */
    public static String capitalizeFirstLetter(final String string) {
        final String returnValue;

        if(string.isEmpty()) {
            returnValue = "";
        } else {
            returnValue = String.format("%s%s",
                    Character.toUpperCase(string.charAt(0)),
                    string.substring(1));
        }

        return returnValue;
    }

    /**
     * Lowercase the first letter of a string.
     *
     * @param string the string
     * @return the string with the first letter lowercased
     */
    public static String lowercaseFirstLetter(final String string) {
        final String returnValue;

        if(string.isEmpty()) {
            returnValue = "";
        } else {
            returnValue = String.format("%s%s",
                    Character.toLowerCase(string.charAt(0)),
                    string.substring(1));
        }

        return returnValue;
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
