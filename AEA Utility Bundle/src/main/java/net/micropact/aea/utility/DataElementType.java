package net.micropact.aea.utility;

import java.util.stream.Stream;

import com.entellitrak.configuration.DataType;

/**
 * This Enum is for the core entellitrak data types.
 *
 * @author zmiller
 */
public enum DataElementType {

    /* TODO: The "underlying classes" variables are not incomplete. */

    /**
     * Text.
     */
    TEXT(1, "Text", DataType.TEXT, String.class),
    /**
     * Number.
     */
    NUMBER(2, "Number", DataType.NUMBER, Integer.class),
    /**
     * Date.
     */
    DATE(3, "Date", DataType.DATE, null),
    /**
     * Currency.
     */
    CURRENCY(4, "Currency", DataType.CURRENCY, null),
    /**
     * Yes/No.
     */
    YES_NO(5, "Yes/No", DataType.YES_NO, null),
    /**
     * File (includes regular, DM and eScan).
     */
    FILE(8, "File", DataType.FILE, null),
    /**
     * State (this is the built-in state field for BTOs).
     */
    STATE(9, "State", DataType.STATE, null),
    /**
     * Passsword.
     */
    PASSWORD(10, "Password", DataType.PASSWORD, null),
    /**
     * Long Text.
     */
    LONG_TEXT(11, "Long Text", DataType.LONG_TEXT, null),
    /**
     * Timestamp.
     */
    TIMESTAMP(12, "Timestamp", DataType.TIMESTAMP, null),
    /**
     * Core uses None to refer to plugins.
     */
    NONE(13, "Plug-in", DataType.NONE, null),
    /**
     * I have not seen this used in entellitrak.
     */
    LONG(14, "Long", DataType.LONG, Long.class);

    private final long etkNumber;
    private final String espIdentifier;
    private final DataType dataType;
    private final Class<?> underlyingClass;

    /**
     * Constructor.
     *
     * @param entellitrakNumber The number which core entellitrak uses to refer to this type of element in the database
     * @param theEspIdentifier The String ESP uses to refer to this data element type.
     * @param theDataType the corresponding entellitrak data type
     * @param theUnderlyingClass This is the class that entellitrak returns from the dynamic object API.
     */
    DataElementType(final long entellitrakNumber, final String theEspIdentifier, final DataType theDataType, final Class<?> theUnderlyingClass){
        etkNumber = entellitrakNumber;
        espIdentifier = theEspIdentifier;
        dataType = theDataType;
        underlyingClass = theUnderlyingClass;
    }

    /**
     * Get the number that core entellitrak uses to refer to this data element.
     *
     * @return The number that core entellitrak uses to refer to this data element
     */
    public long getEntellitrakNumber(){
        return etkNumber;
    }

    /**
     * Get the ESP Identifier..
     *
     * @return the ESP Identifier
     */
    public String getEspIdentifier(){
        return espIdentifier;
    }

    /**
     * Get the data type.
     *
     * @return the data type
     */
    public DataType getDataType(){
        return dataType;
    }

    /**
     * Get the underlying class.
     *
     * @return the class
     */
    public Class<?> getUnderlyingClass(){
        return underlyingClass;
    }

    /**
     * This method converts the core entellitrak number for a data element type into an enum.
     *
     * @param entellitrakNumber A number which entellitrak uses to identify a data element type.
     * @return {@link DataElementType} representing the given entellitrak id.
     */
    public static DataElementType getDataElementType(final long entellitrakNumber){
        return Stream.of(values())
                .filter(type -> type.getEntellitrakNumber() == entellitrakNumber)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("\"%s\" is not a number used by core entellitrak to represent a data type.",
                                entellitrakNumber)));
    }

    /**
     * This method converts the core entellitrak data type to a data element type.
     *
     * @param dataType the data type.
     * @return {@link DataElementType} the data element type.
     */
    public static DataElementType getDataElementType(final DataType dataType){
        return Stream.of(values())
                .filter(type -> type.getDataType() == dataType)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Could not find the data element type corresponding to \"%s\".",
                                dataType)));
    }
}
