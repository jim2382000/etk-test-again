package net.micropact.aea.utility;

import java.util.stream.Stream;

/**
 * Enum representing the possible required levels for a Data Element.
 *
 * @author zmiller
 */
public enum DataElementRequiredLevel {

    /**
     * The element is not required.
     */
    NOT_REQUIRED("1", "Not Required"),
    /**
     * The element was Business Recommended. entellitrak has removed this option.
     */
    BUSINESS_RECOMMENDED("2", "Business Recommended"),
    /**
     * The element is Required.
     */
    REQUIRED("3", "Required");

    private String etkNumber;
    private String display;

    /**
     * Constructor.
     *
     * @param entellitrakNumber The Number entellitrak uses to internally refer to this option.
     * @param theDisplay A user-friendly description of the option
     */
    DataElementRequiredLevel(final String entellitrakNumber, final String theDisplay){
        etkNumber = entellitrakNumber;
        display = theDisplay;
    }

    /**
     * Get the String which entellitrak uses internally to refer to this option.
     * (yes, it is a varchar and not a number).
     *
     * @return The String that entellitrak uses internally to refer to this option.
     */
    public String getEntellitrakNumber(){
        return etkNumber;
    }

    /**
     * Get a user-friendly String representation of this option.
     *
     * @return A user-friendly String representation
     */
    public String getDisplay(){
        return display;
    }

    /**
     * Translate the String that entellitrak uses to refer to the data element required level to the actual
     * required level.
     *
     * @param entellitrakNumber the String entellitrak uses internally to refer to the option.
     * @return the {@link DataElementRequiredLevel}
     */
    public static DataElementRequiredLevel getDataElementRequiredLevel(final String entellitrakNumber){
        return Stream.of(values())
                .filter(type -> type.getEntellitrakNumber().equals(entellitrakNumber))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("\"%s\" is not a number used by core entellitrak to represent a required level.",
                                entellitrakNumber)));
    }
}
