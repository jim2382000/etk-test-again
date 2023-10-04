package net.micropact.aea.core.enums;

import java.util.stream.Stream;

/**
 * This Enum is for the access level type for role data permissions.
 *
 * @author zmiller
 */
public enum DataPermissionAccessLevelType {

    /**
     * No permission.
     */
    NONE(0),
    /**
     * Permission to objects with an explicit assignment.
     */
    USER(1),
    /**
     * Permission to objects within the user's organizational unit.
     */
    ORGANIZATION_UNIT(2),
    /**
     * Permission to objects within the user's organizational unit or child organizational units.
     */
    PARENT_CHILD_ORGANIZATIONAL_UNITS(3),
    /**
     * Global access.
     */
    GLOBAL(4);

    private final long etkNumber;

    /**
     * Constructor.
     *
     * @param entellitrakNumber The number which core entellitrak uses to refer to this type of element in the database
     */
    DataPermissionAccessLevelType(final long entellitrakNumber){
        etkNumber = entellitrakNumber;
    }

    /**
     * Get the number that core entellitrak uses to refer to this data permission access level type.
     *
     * @return The number that core entellitrak uses to refer to this data permission access level type
     */
    public long getEntellitrakNumber(){
        return etkNumber;
    }

    /**
     * This method converts the core entellitrak number for a data permission access level type into an enum.
     *
     * @param entellitrakNumber A number which entellitrak uses to identify a data permission access level type.
     * @return {@link DataPermissionAccessLevelType} representing the given entellitrak id.
     */
    public static DataPermissionAccessLevelType getDataPermissionAccessLevelType(final long entellitrakNumber){

        return Stream.of(values())
                .filter(type -> type.getEntellitrakNumber() == entellitrakNumber)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("\"%s\" is not a number used by core entellitrak to represent a data permission access level.",
                                entellitrakNumber)));
    }
}
