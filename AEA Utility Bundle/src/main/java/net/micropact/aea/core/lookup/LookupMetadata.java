package net.micropact.aea.core.lookup;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataObjectLookupDefinition;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.ListBasedScriptLookupDefinition;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.configuration.LookupDefinitionService;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.ScriptLookupDefinition;
import com.entellitrak.configuration.SystemObjectLookupDefinition;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.lookup.IAeaLookupHandler;
import net.micropact.aea.utility.SystemObjectType;

/**
 * This class contains utility functionality related to lookup metadata.
 *
 * @author zmiller
 */
public final class LookupMetadata {

	/**
	 * Hide constructors for utility classes.
	 */
	private LookupMetadata(){}


	/**
	 * Returns information about the Table and Column that a lookup gets its value from. If it cannot determine the
	 * Table and Column, it will return null.
	 *
	 * @param etk entellitrak executionContext
	 * @param lookupDefinition the lookup definition to get the information about.
	 * @return Table and Column that the lookup gets its Value from.
	 */
	public static TableColumn getLookupReference(final ExecutionContext etk, final LookupDefinition lookupDefinition) {
		try {
			final LookupDefinitionService lookupDefinitionService = etk.getLookupDefinitionService();

			final com.entellitrak.configuration.LookupSourceType lookupSourceType = lookupDefinition.getSourceType();
			final String lookupBusinessKey = lookupDefinition.getBusinessKey();

			String tableName = null;
			String columnName = null;

			switch(lookupSourceType){
			case DATA_OBJECT:
				final DataObjectLookupDefinition dataObjectLookup = lookupDefinitionService.getDataObjectLookupDefinitionByBusinessKey(lookupBusinessKey);

				tableName = dataObjectLookup.getDataObject().getTableName();
				columnName = Optional.ofNullable(dataObjectLookup.getValueElement())
						.map(DataElement::getColumnName)
						.orElse("ID");
				break;
			case SCRIPT:
				final ScriptLookupDefinition scriptLookup = lookupDefinitionService.getScriptLookupDefinitionByBusinessKey(lookupBusinessKey);
				final Script script = scriptLookup.getScript();

				if(Objects.equals(LanguageType.JAVA , script.getLanguageType())){
					final Object javaObject = Class.forName(script.getFullyQualifiedName()).getDeclaredConstructor().newInstance();

					if(javaObject instanceof IAeaLookupHandler){
						final IAeaLookupHandler lookupHandler = (IAeaLookupHandler) javaObject;
						tableName = lookupHandler.getValueTableName(etk);
						columnName = lookupHandler.getValueColumnName(etk);
					}
				}
				break;
			case LIST_BASED_SCRIPT:
				final ListBasedScriptLookupDefinition listBasedLookup = lookupDefinitionService.getListBasedScriptLookupDefinitionByBusinessKey(lookupBusinessKey);
				final Script listBasedScript = listBasedLookup.getScript();

				if(Objects.equals(LanguageType.JAVA, listBasedScript.getLanguageType())){
					final Object javaObject =
							Class.forName(listBasedScript.getFullyQualifiedName()).getDeclaredConstructor().newInstance();

					if(javaObject instanceof IAeaLookupHandler){
						final IAeaLookupHandler lookupHandler = (IAeaLookupHandler) javaObject;
						tableName = lookupHandler.getValueTableName(etk);
						columnName = lookupHandler.getValueColumnName(etk);
					}
				}
				break;
			case SYSTEM_OBJECT:
				final SystemObjectLookupDefinition systemLookup = lookupDefinitionService.getSystemObjectLookupDefinitionByBusinessKey(lookupBusinessKey);
				final SystemObjectType systemObjectType = SystemObjectType.getByLookupSystemObjectType(systemLookup.getSystemObjectType());

				tableName = systemObjectType.getTableName();
				columnName = systemObjectType.getColumnName();
				break;
			default:
				break;
			}

			if(tableName != null && columnName != null){
				return new TableColumn(tableName, columnName);
			}else{
				return null;
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	/**
	 * This class represents a database Table and Column combination.
	 *
	 * @author zmiller
	 */
	public static class TableColumn{

		private final String table;
		private final String column;

		/**
		 * Constructor for TableColumn.
		 *
		 * @param tableName name of the table
		 * @param columnName name of the column
		 */
		TableColumn(final String tableName, final String columnName){
			table = tableName == null ? null : tableName.toUpperCase();
			column = columnName == null ? null : columnName.toUpperCase();
		}

		/**
		 * Get the name of the Table.
		 *
		 * @return name of the table in UPPER case
		 */
		public String getTable(){
			return table;
		}

		/**
		 * Get the name of the Column.
		 *
		 * @return name of the column in UPPER case
		 */
		public String getColumn(){
			return column;
		}
	}
}
