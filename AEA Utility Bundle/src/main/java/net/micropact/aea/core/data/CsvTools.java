package net.micropact.aea.core.data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains various utilities related to reading or writing CSV files.
 *
 * @author Zachary.Miller
 */
public final class CsvTools {

    /**
     * Utility classes do not need public constructors.
     */
    private CsvTools(){}

    /**
     * Encodes a string as a valid CSV String value.
     *
     * @param string string to be encoded.
     * @return A valid CSV value
     */
    public static String encodeCsv(final String string){
        return string == null ? "" :
            String.format("\"%s\"", string.replace("\"", "\"\""));
    }

    /**
     * Encodes a 2-dimensional list of Strings into a CSV file.
     * This makes sense because a CSV file is effectively a 2-dimensional list of Strings.
     *
     * @param rows the 2-dimensional list of Strings
     * @return a CSV string representation of the data
     */
    public static String encodeCsv(final List<List<String>> rows){
        final StringBuilder accumulator = new StringBuilder();

        rows.forEach(row -> {
            /* Suppress platform-specific line ending warning.*/
            @SuppressWarnings("squid:S3457")
            final String rowWithTrailingNewline = String.format("%s\n", encodeRow(row));
            accumulator.append(rowWithTrailingNewline);
        });

        return accumulator.toString();
    }

    /**
     * Encodes a single CSV row (does not include a newline).
     *
     * @param row the row of data
     * @return a CSV encoded single-row
     */
    private static String encodeRow(final List<String> row) {
        final List<String> escapedRow = row.stream()
                .map(CsvTools::encodeCsv)
                .collect(Collectors.toList());

        return join(escapedRow, ",");
    }

    /**
     * Joins a list of Strings with a separator similar to javascript's Array.prototype.join() method.
     *
     * @param strings the list of Strings to join
     * @param separator the value to separate the Strings with
     * @return the joined value
     */
    private static String join(final List<String> strings, final String separator) {
        String returnValue;

        /* We break the algorithm into 2 pieces. One which handles the empty list and one which handles all others.
         * This is done because the algorithm we are using for non-empty lists will throw an exception when it attempts
         * to trim the leading separator. */
        if(strings.isEmpty()){
            returnValue = "";
        }else{
            final StringBuilder accumulator = new StringBuilder();

            strings.forEach(string -> accumulator.append(String.format("%s%s", separator, string)));

            returnValue = accumulator.substring(separator.length());
        }

        return returnValue;
    }
}
