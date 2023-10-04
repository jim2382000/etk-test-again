package net.micropact.aea.du.page.bulkDeleteData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.InputValidationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.WorkExecutionException;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.dynamic.DynamicObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.core.utility.DynamicObjectConfigurationUtils;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.UserUtility;
import net.micropact.aea.utility.Utility;

/**
 * This is a {@link PageController} for a page which deletes all BTOs of a particular type.
 * It uses the underlying core commands (deletWorkflow) and therefore has any limitations of those methods.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class BulkDeleteDataController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		final TextResponse response = etk.createTextResponse();

		final Set<String> keysToDelete = new HashSet<>(Optional.ofNullable(etk.getParameters().getField("dataObjects"))
				.orElse(new ArrayList<>()));

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Bulk Delete Data",
								"page.request.do?page=du.page.bulkDeleteData")));

		if(!keysToDelete.isEmpty()) {
			PageUtility.validateCsrfToken(etk);
		}

		final List<DataObject> dataObjects = dataObjectService.getDataObjects()
				.stream()
				.filter(dataObject -> EtkDataUtils.isRootDataObject(etk, dataObject))
				.sorted(Comparator.comparing(
						DataObject::getObjectType)
						.thenComparing(DataObject::getLabel)
						.thenComparing(DataObject::getBusinessKey))
				.collect(Collectors.toList());


		/* We will delete any objects we were requested to */
		dataObjects
		.stream()
		.filter(dataObject -> keysToDelete.contains(dataObject.getBusinessKey()))
		.forEachOrdered(dataObject -> {
			try {
				deleteObjectType(etk, dataObject);
			} catch (final ClassNotFoundException e) {
				throw new GeneralRuntimeException(e);
			}
		});

		final Gson gson = new Gson();

		/* We add the counts. This has to be done after the deletions happen so that the counts are 0
		 * for ones just deleted */
		response.put("dataObjects", gson.toJson(dataObjects
				.stream()
				.map(dataObject -> {
					try {
						return Utility.arrayToMap(String.class, Object.class, new Object[][]{
							{"OBJECT_TYPE", dataObject.getObjectType().name()},
							{"BUSINESS_KEY", dataObject.getBusinessKey()},
							{"LABEL", dataObject.getLabel()},
							{"count", etk.createSQL("SELECT COUNT(*) FROM "+ dataObject.getTableName()).fetchLong()},
						});
					} catch (final IncorrectResultSizeDataAccessException e) {
						throw new GeneralRuntimeException(e);
					}
				})
				.collect(Collectors.toList())));

		response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

		return response;
	}

	/**
	 * Deletes all data objects of a particular type.
	 *
	 * @param etk entellitrak execution context
	 * @param dataObject the data object
	 * @throws ClassNotFoundException If there was an underlying {@link ClassNotFoundException}
	 */
	private static void deleteObjectType(final ExecutionContext etk,
			final DataObject dataObject) throws ClassNotFoundException{
		final Class<? extends DataObjectInstance> objectClass = DynamicObjectConfigurationUtils.getDynamicClass(etk, dataObject);

		getIdsForDataObject(etk, dataObject)
		.forEachOrdered(trackingId -> {
			try {
				etk.doWork(etkWork -> {
					final DynamicObjectService objectService = etkWork.getDynamicObjectService();
					try {
						objectService.createDeleteOperation(objectService.get(objectClass, trackingId))
						.setUser(UserUtility.getCurrentUserOrServiceAccount(etk))
						.delete();
					} catch (final InputValidationException e) {
						throw new ApplicationException(e);
					}
				});
			} catch (final WorkExecutionException e) {
				etk.getLogger().error(String.format("Error attempting to delete object of type %s with trackingId %s",
						dataObject,
						trackingId), e);
			}
		});
	}

	/**
	 * Retrieves the trackingIds of all objects of a particular type.
	 *
	 * @param etk entellitrak execution
	 * @param dataObjectType the type of object to get ids for
	 * @return the tracking ids
	 */
	private static Stream<Long> getIdsForDataObject(final ExecutionContext etk, final DataObject dataObjectType){
		return QueryUtility.mapsToLongs(etk.createSQL(String.format("SELECT id FROM %s ORDER BY id",
				dataObjectType.getTableName()))
				.fetchList())
				.stream();
	}
}
