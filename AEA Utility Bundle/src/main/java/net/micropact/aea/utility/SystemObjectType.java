package net.micropact.aea.utility;

import java.util.Objects;
import java.util.stream.Stream;

import com.entellitrak.configuration.LookupSystemObjectType;

/**
 * This enum represents the possible System Objects usable by Lookups with Lookup Source System Object.
 *
 * @author zmiller
 */
public enum SystemObjectType {

    /**
     * ETK_USER.
     */
    USER(1, LookupSystemObjectType.USER, "User", "ETK_USER", "USER_ID");

    private final long id;
    private final LookupSystemObjectType lookupSystemObjectType;
    private final String name;
    private final String tableName;
    private final String columnName;

    /**
     * Constructor.
     *
     * @param anId number which entellitrak uses internally to represent this System Object Type
     * @param theLookupSystemObjectType the entellitrak lookup system object type
     * @param aName User-friendly String to use for this System Object Type
     * @param aTableName The table from which the lookup gets its value
     * @param aColumnName The column from which the lookup gets its value
     */
    SystemObjectType(final long anId, final LookupSystemObjectType theLookupSystemObjectType, final String aName, final String aTableName, final String aColumnName){
        id = anId;
        lookupSystemObjectType = theLookupSystemObjectType;
        name = aName;
        tableName = aTableName;
        columnName = aColumnName;
    }

    /**
     * Get the number which entellitrak uses internally to represent this System Object Type.
     *
     * @return number which entellitrak uses internally to represent this System Object Type
     */
    public long getId(){
        return id;
    }

    /**
     * Get a user-friendly display of the System Object Type.
     *
     * @return User-friendly String to use for this System Object Type
     */
    public String getName(){
        return name;
    }

    /**
     * Translate the number entellitrak uses to refer to a System Object Type to the actual System Object Type.
     *
     * @param id number which entellitrak uses internally to represent this System Object Type
     * @return The System Object Type represented by the id
     */
    public static SystemObjectType getById(final long id){
        return Stream.of(values())
                .filter(value -> Objects.equals(id, value.getId()))
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find SystemObjectType for id: %s", id)));
    }

    /**
     * Translate the entellitrak {@link LookupSystemObjectType} to a {@link SystemObjectType}.
     *
     * @param lookupSystemObjectType the entellitrak lookup system object type
     * @return the system object type
     */
    public static SystemObjectType getByLookupSystemObjectType(final LookupSystemObjectType lookupSystemObjectType) {
        return Stream.of(values())
                .filter(value -> Objects.equals(lookupSystemObjectType, value.lookupSystemObjectType))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find SystemObjectType for lookup system object type: %s", lookupSystemObjectType)));
    }

    /**
     * Get the table name which the lookup gets its value from.
     *
     * @return The table which the lookup gets its value from.
     */
    public String getTableName(){
        return tableName;
    }

    /**
     * Get the column which the lookup gets its value from.
     *
     * @return The column which the lookup gets its value from.
     */
    public String getColumnName(){
        return columnName;
    }
}
