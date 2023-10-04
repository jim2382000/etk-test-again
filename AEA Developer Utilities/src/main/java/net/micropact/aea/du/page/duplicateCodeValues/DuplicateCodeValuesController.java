package net.micropact.aea.du.page.duplicateCodeValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataType;
import com.entellitrak.configuration.ObjectType;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This page gets information about all records in the system which have a value in the code element which is the same
 * as another record of the same type.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class DuplicateCodeValuesController implements PageController {

	private static final Set<DataType> CODE_DATA_TYPES = Collections.unmodifiableSet(EnumSet.of(DataType.TEXT, DataType.LONG_TEXT));

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Duplicate Code Values",
								"page.request.do?page=du.page.duplicateCodeValues")));

		response.put("dataObjects", new Gson().toJson(getDataObjects(etk)
				.stream()
				.map(dataObjectDuplicate -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
					{"name", dataObjectDuplicate.getName()},
					{"objectType", dataObjectDuplicate.getDataObjectType()},
					{"duplicates", dataObjectDuplicate.getDuplicates()
						.stream()
						.map(duplicate ->
						Utility.arrayToMap(String.class, Object.class, new Object[][]{
							{"code", duplicate.getCode()},
							{"duplicateObjects", duplicate.getDuplicateObjects()
								.stream()
								.map(duplicateObject -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
									{"id", duplicateObject.id},
									{"url", duplicateObject.getUrl()},
								}))
								.collect(Collectors.toList())},
						}))
						.collect(Collectors.toList())},
				}))
				.collect(Collectors.toList())));

		return response;
	}

	/**
	 * Gets all of the objects which have duplicate values.
	 *
	 * @param etk Entellitrak Execution Context
	 * @return The list (sorted by # of duplicates and then by name) of duplicate data objects.
	 */
	public static List<DataObjectDuplicates> getDataObjects(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();

		return dataObjectService.getDataObjects()
				.stream()
				.map(dataObject -> dataElementService.getDataElements(dataObject)
						.stream()
						.filter(dataElement
								-> "code".equals(dataElement.getPropertyName())
								&& CODE_DATA_TYPES.contains(dataElement.getDataType()))
						.findAny())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(dataElement -> new DataObjectDuplicates(etk, dataElement))
				.filter(dataObjectDuplicates -> !dataObjectDuplicates.getDuplicates().isEmpty())
				.sorted(Comparator.comparing((final DataObjectDuplicates dataObjectDuplicate) -> dataObjectDuplicate.getDuplicates().size())
						.thenComparing(DataObjectDuplicates::getName))
				.collect(Collectors.toList());
	}

	/**
	 * This class represents a Data Object and all of the duplicate records.
	 *
	 * @author zmiller
	 */
	public static class DataObjectDuplicates {

		private DataElement dataElement;
		private final List<Duplicate> duplicates;

		/**
		 * This constructor will generate a record for the data object and find all of the duplicates.
		 * The reason that the constructor takes so many 'duplicate' parameters is to keep it from having to
		 * do an additional query.
		 *
		 * @param etk Entellitrak Execution Context.
		 * @param theDataElement the data element
		 */
		public DataObjectDuplicates(final ExecutionContext etk,
				final DataElement theDataElement) {
			dataElement = theDataElement;

			final String columnName = dataElement.getColumnName();
			final DataObject dataObject = dataElement.getDataObject();
			final String tableName = dataObject.getTableName();

			final ArrayList<Map<String, Object>> objectInfos =
					new ArrayList<>(etk.createSQL("SELECT grouped.ID, grouped.CODE, grouped.COUNT FROM ( SELECT ID, "+columnName+" CODE, COUNT(*) OVER (PARTITION BY "+columnName+") COUNT FROM "+tableName+" ) grouped WHERE grouped.COUNT > 1 ORDER BY grouped.COUNT DESC, grouped.CODE, grouped.ID")
							.fetchList());

			duplicates = new ArrayList<>();

			int index = 0;

			while (index < objectInfos.size()){

				final Map<String, Object> object = objectInfos.get(index);
				final String codeValue = (String) object.get("CODE");
				final long count = ((Number) object.get("COUNT")).longValue();

				final List<DuplicateObject> duplicateDataObjects = new ArrayList<>();

				for(int i = index; i < index + count;i++){
					duplicateDataObjects.add(new DuplicateObject(((Number) objectInfos.get(i).get("ID")).longValue(),
							dataObject));
				}

				duplicates.add(new Duplicate(codeValue, duplicateDataObjects));
				index += count;
			}

			Collections.sort(duplicates, (o1, o2) -> {
				final int firstComp = Integer.compare(o1.getDuplicateObjects().size(), o2.getDuplicateObjects().size());
				if(firstComp != 0){
					return firstComp;
				}else{
					return o1.getCode().compareTo(o2.getCode());
				}
			});
		}

		/**
		 * Gets the Data Object's name.
		 *
		 * @return The data object name.
		 */
		public String getName(){
			return dataElement.getDataObject().getName();
		}

		/**
		 * Gets the list of duplicate codes.
		 *
		 * @return List of duplicate codes.
		 */
		public List<Duplicate> getDuplicates(){
			return duplicates;
		}

		/**
		 * Gets the data object type.
		 *
		 * @return The Data Object Type
		 */
		public ObjectType getDataObjectType(){
			return dataElement.getDataObject().getObjectType();
		}
	}

	/**
	 * This class represents a particular duplicate code value.
	 * It also stores the list of records which actually have the duplicate value.
	 *
	 * @author zmiller
	 */
	public static class Duplicate {

		private final String code;
		private final List<DuplicateObject> duplicateObjects;

		/**
		 * Constructor for Duplicate.
		 *
		 * @param codeValue Value of the code element which this duplicate represents.
		 * @param duplicateDataObjects A list of data objects which have codeValue as their code.
		 */
		Duplicate(final String codeValue, final List<DuplicateObject> duplicateDataObjects){
			code = codeValue;
			duplicateObjects = duplicateDataObjects;
		}

		/**
		 * Gets the Code.
		 *
		 * @return The code this duplicate represents.
		 */
		public String getCode(){
			return code;
		}

		/**
		 * Gets the list of records which match the code.
		 *
		 * @return List of Records which have this code.
		 */
		public List<DuplicateObject> getDuplicateObjects(){
			return duplicateObjects;
		}
	}

	/**
	 * Represents a single duplicate record.
	 *
	 * @author zmiller
	 */
	public static class DuplicateObject {

		private final long id;
		private final DataObject dataObject;

		/**
		 * Constructor for DuplicateObject.
		 *
		 * @param trackingId Tracking Id of the record.
		 * @param theDataObject the data object
		 */
		public DuplicateObject(final long trackingId, final DataObject theDataObject){
			id = trackingId;
			dataObject = theDataObject;
		}

		/**
		 * Gets a relative URL to access this particular record.
		 *
		 * @return Relative URL to open up this particular record.
		 */
		private String getUrl(){
			final String businessKey = dataObject.getBusinessKey();
			final ObjectType objectType = dataObject.getObjectType();

			switch (objectType) {
			case TRACKING:
				return String.format("workflow.do?dataObjectKey=%s&trackingId=%s",
						businessKey, id);
			case REFERENCE:
				return String.format("admin.refdata.update.request.do?dataObjectKey=%s&trackingId=%s",
						businessKey, id);
			default:
				throw new GeneralRuntimeException(String.format("NonExhaustive Pattern: No matching data object type: %s ", objectType));
			}
		}
	}
}
