package net.micropact.aea.du.page.dataDictionaryDownload;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;

import net.micropact.aea.core.data.CsvTools;
import net.micropact.aea.utility.DataElementRequiredLevel;
import net.micropact.aea.utility.DataElementType;
import net.micropact.aea.utility.Utility;

/**
 * This class serves as the controller code for a page which can generate a Data Dictionary of the objects and elements
 * within entellitrak.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DataDictionaryDownloadController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		return etk.createFileResponse("dataDictionary.csv", new ByteArrayInputStream(generateCsv(etk).getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Generates a CSV file containing the data dictionary of the objects in the system.
	 *
	 * @param etk entellitrak execution context
	 * @return The CSV file
	 */
	private static String generateCsv(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		final Stream<Map<String, Object>> elementInfos = getDataObjectElementPairs(etk)
				.sorted(Comparator.comparing((final DataObjectElementPair pair) -> pair.getDataObject().getLabel())
						.thenComparing(pair -> pair.getDataObject().getBusinessKey())
						.thenComparing(pair -> pair.getDataElement().orElse(null),
								Comparator.nullsFirst(Comparator.comparing(DataElement::getName)
										.thenComparing(DataElement::getBusinessKey))))
		.map(objectElementPair -> {
			final DataObject dataObject = objectElementPair.getDataObject();
			final Optional<DataElement> dataElement = objectElementPair.getDataElement();

			return Utility.arrayToMap(String.class, Object.class, new Object[][] {
				{ "OBJECT_LABEL", dataObject.getLabel()},
				{ "OBJECT_BUSINESS_KEY", dataObject.getBusinessKey()},
				{ "OBJECT_DESCRIPTION", dataObject.getDescription() },
				{ "OBJECT_TABLE", dataObject.getTableName() },
				{ "PARENT_BUSINESS_KEY", Optional.ofNullable(dataObjectService.getParent(dataObject)).map(DataObject::getBusinessKey).orElse(null) },
				{ "ELEMENT_NAME", dataElement.map(DataElement::getName).orElse(null)},
				{ "ELEMENT_BUSINESS_KEY", dataElement.map(DataElement::getBusinessKey).orElse(null)},
				{ "COLUMN_NAME", dataElement.map(DataElement::getColumnName).orElse(null)},
				{ "ELEMENT_DESCRIPTION", dataElement.map(DataElement::getDescription).orElse(null)},
				{ "REQUIRED", dataElement.map(DataElement::isRequired).map(required -> (required.booleanValue() ? DataElementRequiredLevel.REQUIRED : DataElementRequiredLevel.NOT_REQUIRED).getDisplay()).orElse(null)},
				{ "DATA_TYPE", dataElement.map(element -> DataElementType.getDataElementType(element.getDataType()).getEspIdentifier()).orElse(null)},
				{ "ELEMENT_TABLE", dataElement.map(DataElement::getTableName).orElse(null)},
				{ "LOOKUP_NAME", dataElement.map(DataElement::getLookup).map(LookupDefinition::getName).orElse(null)},
				{ "LOOKUP_BUSINESS_KEY", dataElement.map(DataElement::getLookup).map(LookupDefinition::getBusinessKey).orElse(null)}});
		});

		final String[][] csvConfiguration = {
				{ "Object Label", "OBJECT_LABEL" },
				{ "Object Business Key", "OBJECT_BUSINESS_KEY" },
				{ "Object Description", "OBJECT_DESCRIPTION" },
				{ "Object Table", "OBJECT_TABLE" },
				{ "Parent Object Business Key", "PARENT_BUSINESS_KEY" },
				{ "Element Name", "ELEMENT_NAME" },
				{ "Element Business Key", "ELEMENT_BUSINESS_KEY" },
				{ "Element Column", "COLUMN_NAME" },
				{ "Element Description", "ELEMENT_DESCRIPTION" },
				{ "Required", "REQUIRED" },
				{ "Data Type", "DATA_TYPE" },
				{ "Element Table", "ELEMENT_TABLE" },
				{ "Lookup Name", "LOOKUP_NAME" },
				{ "Lookup Business Key", "LOOKUP_BUSINESS_KEY" } };

		final String[] headers = extractArrayColumn(csvConfiguration, 0);
		final String[] mapKeys = extractArrayColumn(csvConfiguration, 1);

		final List<List<String>> csvData = new ArrayList<>();

		final List<String> formattedHeaders = Stream.of(headers)
				.map(header -> String.format("*%s*", header))
				.collect(Collectors.toList());

		csvData.add(formattedHeaders);

		elementInfos.forEach(elementInfo -> {
			final List<String> rowData = Stream.of(mapKeys)
					.map(key -> (String) elementInfo.get(key))
					.collect(Collectors.toList());

			csvData.add(rowData);
		});

		return CsvTools.encodeCsv(csvData);
	}

	private static Stream<DataObjectElementPair> getDataObjectElementPairs(final ExecutionContext etk){
		final DataElementService dataElementService = etk.getDataElementService();
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects()
		.stream()
		.flatMap(dataObject -> {
			final Collection<DataElement> dataElements = dataElementService.getDataElements(dataObject);

			if(dataElements.isEmpty()) {
				return Stream.of(new DataObjectElementPair(dataObject, Optional.empty()));
			} else {
				return dataElements.stream()
						.map(dataElement -> new DataObjectElementPair(dataObject, Optional.of(dataElement)));
			}
		});
	}

	/**
	 * Extracts a column from a 2-dimensional array where the "column" is the 2nd dimension.
	 *
	 * @param array
	 *            two dimensional array to extract the colum out of. The column is the 2nd dimension
	 * @param column
	 *            The column to extract (zero-indexed)
	 * @return The ordered column values
	 */
	private static String[] extractArrayColumn(final String[][] array, final int column) {
		final String[] returnValue = new String[array.length];

		for (int i = 0; i < array.length; i++) {
			returnValue[i] = array[i][column];
		}

		return returnValue;
	}

	private static class DataObjectElementPair {

		private final DataObject dataObject;
		private final Optional<DataElement> dataElement;

		public DataObjectElementPair(final DataObject theDataObject, final Optional<DataElement> theDataElement) {
			dataObject = theDataObject;
			dataElement = theDataElement;
		}

		public DataObject getDataObject() {
			return dataObject;
		}

		public Optional<DataElement> getDataElement() {
			return dataElement;
		}
	}
}
