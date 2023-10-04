package net.micropact.aea.utility;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * enum representing the types of lookups in an entellitrak system.
 *
 * @author zmiller
 */
public enum LookupSourceType {

	/**
	 * Data Object.
	 */
	DATA_OBJECT_LOOKUP(1, "Data Object", com.entellitrak.configuration.LookupSourceType.DATA_OBJECT),
	/**
	 * SQL.
	 */
	QUERY_LOOKUP(2, "SQL Query", com.entellitrak.configuration.LookupSourceType.SQL_QUERY),
	/**
	 * I have never seen this used by entellitrak.
	 */
	PLUGIN_LOOKUP(3, "Plugin", null),
	/**
	 * Script.
	 */
	SCRIPT_LOOKUP(4, "Script", com.entellitrak.configuration.LookupSourceType.SCRIPT),
	/**
	 * System.
	 */
	SYSTEM_OBJECT_LOOKUP(5, "System Object", com.entellitrak.configuration.LookupSourceType.SYSTEM_OBJECT),
	/**
	 * List-Based Script.
	 * */
	LIST_BASED_SCRIPT_LOOKUP(6, "List-Based Script", com.entellitrak.configuration.LookupSourceType.LIST_BASED_SCRIPT);

	private final long etkNumber;
	private final String display;
	private final com.entellitrak.configuration.LookupSourceType coreLookupSourceType;

	/**
	 * Constructor.
	 *
	 * @param entellitrakNumber The number which core entellitrak uses to refer to this type of element in the database
	 * @param displayString User-friendly representation of the Lookup Source Type
	 * @param theCoreLookupSourceType the core lookup source type
	 */
	LookupSourceType(final long entellitrakNumber, final String displayString, final com.entellitrak.configuration.LookupSourceType theCoreLookupSourceType){
		etkNumber = entellitrakNumber;
		display = displayString;
		coreLookupSourceType = theCoreLookupSourceType;
	}

	/**
	 * Get the number that entellitrak uses internally to refer to this Lookup Source Type.
	 *
	 * @return The number that core entellitrak uses to refer to this data object type.
	 */
	public long getEntellitrakNumber(){
		return etkNumber;
	}

	/**
	 * Get the user-friendly display.
	 *
	 * @return A user-friendly representation of the Lookup Source Type
	 */
	public String getDisplay(){
		return display;
	}

	public com.entellitrak.configuration.LookupSourceType getCoreLookupSourceType() {
		return coreLookupSourceType;
	}

	/**
	 * This method converts the core entellitrak number for a data object type into an enum.
	 *
	 * @param entellitrakNumber A number which entellitrak uses to identify a data object type.
	 * @return {@link DataObjectType} representing the given entellitrak id.
	 */
	public static LookupSourceType getLookupSourceType(final long entellitrakNumber){
		return Stream.of(values())
				.filter(type -> type.getEntellitrakNumber() == entellitrakNumber)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("\"%s\" is not a number used by core entellitrak to represent a lookup type.",
								entellitrakNumber)));
	}

	public static LookupSourceType getLookupSourceTypeByCoreLookupSourceType(final com.entellitrak.configuration.LookupSourceType sourceType) {
		if(Objects.isNull(sourceType)) {
			throw new IllegalArgumentException("sourceType cannot be null");
		} else {
			return Stream.of(values())
					.filter(type -> Objects.equals(type.coreLookupSourceType, sourceType))
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("\"%s\" is not a recognized sourceType",
									sourceType)));
		}
	}
}
