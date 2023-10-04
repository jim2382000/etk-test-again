package net.micropact.aea.utility;

import java.util.Objects;
import java.util.stream.Stream;

import com.entellitrak.configuration.LookupDisplayFormat;

/**
 * This enum represents the display formats that entellitrak uses for Lookups of type System Object.
 *
 * @author zachary.miller
 */
public enum SystemObjectDisplayFormat {

    /**
     * Display just the username (jdoe).
     */
    ACCOUNT_NAME(1, LookupDisplayFormat.USER_ACCOUNT_NAME, "Account Name"),
    /**
     * Display LastName, FirstName, MI (Doe, John C).
     */
    LASTNAME_FIRSTNAME_MI(2, LookupDisplayFormat.USER_LAST_FIRST_MIDDLE_NAME, "LastName, FirstName MI");

    private final long id;
    private final LookupDisplayFormat lookupDisplayFormat;
    private final String name;

    /**
     * Constructor.
     *
     * @param anId the id entellitrak uses internally to refer to this display format
     * @param theLookupDisplayFormat the core lookup display format
     * @param aName A user-friendly representation of the display format.
     */
    SystemObjectDisplayFormat(final long anId, final LookupDisplayFormat theLookupDisplayFormat, final String aName){
        id = anId;
        lookupDisplayFormat = theLookupDisplayFormat;
        name = aName;
    }

    /**
     * Get the id.
     *
     * @return the id
     */
    public long getId(){
        return id;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * Translates the number entellitrak uses internally to refer to a display format to a
     * {@link SystemObjectDisplayFormat}.
     *
     * @param id id entellitrak uses to refer to this display format.
     * @return the {@link SystemObjectDisplayFormat}
     */
    public static SystemObjectDisplayFormat getById(final long id){
        return Stream.of(values())
                .filter(value -> id == value.getId())
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find SystemObjectDisplayFormat for id: %s", id)));
    }

    /**
     * Get the system object display format for a given core lookup display format.
     *
     * @param lookupDisplayFormat the core lookup display format
     * @return the system object display format
     */
    public static SystemObjectDisplayFormat getBySystemObjectDisplayFormat(
            final LookupDisplayFormat lookupDisplayFormat) {
        return Stream.of(values())
                .filter(value -> Objects.equals(lookupDisplayFormat, value.lookupDisplayFormat))
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find SystemObjectDisplayFormat for LookupDisplayFormat: %s", lookupDisplayFormat)));
    }
}
