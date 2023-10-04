package net.micropact.aea.du.page.foreignKeysCsv;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.FileResponse;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;

import net.entellitrak.aea.gl.api.etk.DataObjectUtil;
import net.micropact.aea.core.data.CsvTools;
import net.micropact.aea.core.lookup.LookupMetadata;
import net.micropact.aea.core.lookup.LookupMetadata.TableColumn;

/**
 * This class contains the controller code for a page which will generate a CSV file containing all the foreign keys
 * for tables defined within the application (ie: it does not include ETK_ or JPBM_ tables). For columns that it
 * knows are foreign keys, but does not know what they point to, the CSV will contain them, but have blank entries.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class ForeignKeyCsvController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final FileResponse response = etk.createFileResponse("foreign_keys.csv",
				new ByteArrayInputStream(
						encodeCsv(getAllForeignKeys(etk))
						.getBytes(StandardCharsets.UTF_8)));

		response.setContentType("text/csv");

		return response;
	}

	/**
	 * Compares two Strings, either of which may be null. nulls are less than any other String.
	 *
	 * @param s1 first string
	 * @param s2 second string
	 * @return a negative number if s1 &lt; s2, zero if s1 equals s2, a positive number if s1 &gt; s2
	 */
	static int compareNullSafe(final String s1, final String s2){
		if(s1 == null && s2 == null){
			return 0;
		}else if(s1 == null){
			return -1;
		}else if(s2 == null){
			return 1;
		}else{
			return s1.compareTo(s2);
		}
	}

	/**
	 * Encodes key representations into a valid CSV String.
	 *
	 * @param keyRepresentations a list of key representations to be converted to CSV
	 * @return a String representing a CSV.
	 */
	private static String encodeCsv(final List<KeyRepresentation> keyRepresentations){
		sort(keyRepresentations);

		final StringBuilder builder = new StringBuilder();

		builder.append("*Child Table*,*Child Column*,*Parent Table*,*Parent Column*,*Add Foreign Key (These keys should be dropped immediately after creation of the ERD)*,*Drop Foreign Key (These will NOT match if you have added lookups since creating the keys)*\n");

		long foreignKeyCurrentIndex = 0;

		for(final KeyRepresentation keyRepresentation : keyRepresentations){
			if(keyRepresentation.hasReference()){
				foreignKeyCurrentIndex += 1;
			}

			builder.append(encodeCsv(keyRepresentation, foreignKeyCurrentIndex));
		}

		return builder.toString();
	}

	/**
	 * Encodes a key representation as a row for a CSV file.
	 *
	 * @param keyRepresentation a key representation to be converted to a CSV row.
	 * @param foreignKeyCurrentIndex The number of this foreign key in our CSV file
	 * @return A row (with trailing newline) for a CSV file.
	 */
	private static String encodeCsv(final KeyRepresentation keyRepresentation, final long foreignKeyCurrentIndex){
		/* Suppress platform-specific line ending warning.*/
		@SuppressWarnings("squid:S3457")
		final String csvRow = String.format("%s,%s,%s,%s,%s,%s\n", CsvTools.encodeCsv(keyRepresentation.getChildTable()),
				CsvTools.encodeCsv(keyRepresentation.getChildColumn()),
				CsvTools.encodeCsv(keyRepresentation.getParentTable()),
				CsvTools.encodeCsv(keyRepresentation.getParentColumn()),
				CsvTools.encodeCsv(generateCreateForeignKeyStatement(keyRepresentation, foreignKeyCurrentIndex)),
				CsvTools.encodeCsv(dropForeignKey(keyRepresentation, foreignKeyCurrentIndex)));

		return csvRow;
	}

	/**
	 * Generates the name to use for a particular Foreign Key.
	 *
	 * @param foreignKeyNumber The number of the key as it appears in the CSV
	 * @return The name of the index to use for the foreign key
	 */
	private static String generateForeignKeyName(final long foreignKeyNumber){
		return String.format("ETK_FK_%s", foreignKeyNumber);
	}

	/**
	 * Generates a SQL statement to add a foreign key constraint.
	 *
	 * @param keyRepresentation The information regarding the key which is to be created
	 * @param foreignKeyNumber The number of the key to be created.
	 * @return An SQL statement to add the foreign key constraint
	 */
	private static String generateCreateForeignKeyStatement (final KeyRepresentation keyRepresentation,
			final long foreignKeyNumber) {
		return keyRepresentation.hasReference()
				? String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s);",
						keyRepresentation.getChildTable(),
						generateForeignKeyName(foreignKeyNumber),
						keyRepresentation.getChildColumn(),
						keyRepresentation.getParentTable(),
						keyRepresentation.getParentColumn())
						: null;
	}

	/**
	 * Generates a SQL statement to drop the foreign key.
	 *
	 * @param keyRepresentation The data describing the key to be dropped
	 * @param foreignKeyNumber The number of this key in our CSV file
	 * @return An SQL statement to drop the foreign key constraint
	 */
	private static String dropForeignKey (final KeyRepresentation keyRepresentation, final long foreignKeyNumber) {
		return keyRepresentation.hasReference()
				? String.format("ALTER TABLE %s DROP CONSTRAINT %s;",
						keyRepresentation.getChildTable(),
						generateForeignKeyName(foreignKeyNumber))
						: null;
	}

	/**
	 * Sorts a list of key representations in order to be more user-friendly.
	 *
	 * @param keyRepresentations key representations to be sorted
	 */
	private static void sort(final List<KeyRepresentation> keyRepresentations){
		Collections.sort(keyRepresentations, (o1, o2) -> compareNullSafe(o1.getParentColumn(), o2.getParentColumn()));
		Collections.sort(keyRepresentations, (o1, o2) -> compareNullSafe(o1.getParentTable(), o2.getParentTable()));
		Collections.sort(keyRepresentations, (o1, o2) -> compareNullSafe(o1.getChildColumn(), o2.getChildColumn()));
		Collections.sort(keyRepresentations, (o1, o2) -> compareNullSafe(o1.getChildTable(), o2.getChildTable()));
		Collections.sort(keyRepresentations, (o1, o2) -> (o1.getParentColumn() == null ? "a" : "b")
				.compareTo(o2.getParentColumn() == null ? "a" : "b"));
	}

	/**
	 * Gets information regarding all foreign keys in user-configurable code within entellitrak.
	 * This includes:
	 *  <ul>
	 *      <li>ID_PARENT</li>
	 *      <li>ID_BASE</li>
	 *      <li>Lookups</li>
	 *      <li>Multiselects</li>
	 *  </ul>
	 *
	 * @param etk entellitrak execution context
	 * @return A list of key representations.
	 */
	private static List<KeyRepresentation> getAllForeignKeys(final ExecutionContext etk) {
		final List<KeyRepresentation> returnList = new ArrayList<>();

		returnList.addAll(getParentIds(etk));
		returnList.addAll(getBaseIds(etk));
		returnList.addAll(getLookupsSingle(etk));
		returnList.addAll(getLookupsMulti(etk));

		return returnList;
	}

	/**
	 * Gets the foreign keys arising from entellitrak multiselects. Each multiselect creates two foreign keys.
	 * One is the id_owner of the m_ table, the other is the final column on the m_ table.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of key representations.
	 */
	private static List<KeyRepresentation> getLookupsMulti(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();

		return dataObjectService.getDataObjects()
				.stream()
				.flatMap(dataObject -> dataElementService.getDataElements(dataObject)
						.stream()
						.filter(dataElement -> (Objects.equals(true, dataElement.isBoundToLookup())
								&& Objects.equals(true, dataElement.isMultiValued())))
						.flatMap(dataElement -> {
							final TableColumn tableColumn =
									LookupMetadata.getLookupReference(etk, dataElement.getLookup());

							return Stream.of(
									new KeyRepresentation(
											dataObject.getTableName(),
											"ID",
											dataElement.getTableName(),
											"ID_OWNER"),
									new KeyRepresentation(
											tableColumn == null ? null: tableColumn.getTable(),
													tableColumn == null ? null : tableColumn.getColumn(),
															dataElement.getTableName(),
															dataElement.getColumnName()));
						}))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the foreign keys in entellitrak arising from single selects within the system.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of key representations.
	 */
	private static List<KeyRepresentation> getLookupsSingle(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();

		return dataObjectService.getDataObjects().stream()
				.flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream()
						.filter(dataElement ->
						Objects.equals(dataElement.isBoundToLookup(), true)
						&& Objects.equals(false, dataElement.isMultiValued()))
						.map(dataElement -> {
							final LookupDefinition lookupDefinition = dataElement.getLookup();

							final TableColumn tableColumn =
									LookupMetadata.getLookupReference(etk, lookupDefinition);

							return new KeyRepresentation(
									tableColumn == null ? null : tableColumn.getTable(),
											tableColumn == null ? null : tableColumn.getColumn(),
													dataObject.getTableName(),
													dataElement.getColumnName());
						}))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list containing information regarding all foreign keys within entellitrak arising from ID_BASE of CTOs.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of key representations
	 */
	private static List<KeyRepresentation> getBaseIds(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects()
				.stream()
				.filter(dataObject -> !DataObjectUtil.isBaseDataObject(etk, dataObject))
				.map(dataObject -> new KeyRepresentation(DataObjectUtil.getBaseObject(etk, dataObject).getTableName(), "ID", dataObject.getTableName(), "ID_BASE"))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list containing information regarding all foreign keys within entellitrak arising from the ID_PARENT
	 * of CTOs.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of foreign keys
	 */
	private static List<KeyRepresentation> getParentIds(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects()
				.stream()
				.filter(dataObject -> !DataObjectUtil.isBaseDataObject(etk, dataObject))
				.map(dataObject -> new KeyRepresentation(dataObjectService.getParent(dataObject).getTableName(), "ID", dataObject.getTableName(), "ID_PARENT"))
				.collect(Collectors.toList());
	}

	/**
	 * This class represents a foreign key relationship.
	 * It contains information on the table and column for each side of the key.
	 * The "child" is the column which actually has the constraint. The "parent" is the column
	 * which the child points to.
	 *
	 * @author zmiller
	 */
	private static final class KeyRepresentation {
		private final String theParentTable;
		private final String theParentColumn;
		private final String theChildTable;
		private final String theChildColumn;

		/**
		 * Constructs a new KeyRepresentation (child points to parent).
		 *
		 * @param parentTable The table of the parent
		 * @param parentColumn The column of the parent
		 * @param childTable The table of the child
		 * @param childColumn The column of the child
		 */
		KeyRepresentation(final String parentTable,
				final String parentColumn,
				final String childTable,
				final String childColumn){
			theParentTable = Optional.ofNullable(parentTable).map(String::toUpperCase).orElse(null);
			theParentColumn = Optional.ofNullable(parentColumn).map(String::toUpperCase).orElse(null);
			theChildTable = Optional.ofNullable(childTable).map(String::toUpperCase).orElse(null);
			theChildColumn = Optional.ofNullable(childColumn).map(String::toUpperCase).orElse(null);
		}

		/**
		 * Get the parent table.
		 *
		 * @return The table of the parent of the key
		 */
		public String getParentTable(){
			return theParentTable;
		}

		/**
		 * Get the parent column.
		 *
		 * @return The column of the parent of the key
		 */
		public String getParentColumn(){
			return theParentColumn;
		}

		/**
		 * Get the child table.
		 *
		 * @return The table of the child of the key
		 */
		public String getChildTable(){
			return theChildTable;
		}

		/**
		 * Get the child column.
		 *
		 * @return The column of the child of the key
		 */
		public String getChildColumn(){
			return theChildColumn;
		}

		/**
		 * TODO: This should attempt to do better when it comes to eliminating Views or non-unique columns.
		 *
		 * @return Whether they key actually has a reference to a table/column combination or not
		 */
		public boolean hasReference(){
			return theParentTable != null && theChildTable != null;
		}
	}
}
