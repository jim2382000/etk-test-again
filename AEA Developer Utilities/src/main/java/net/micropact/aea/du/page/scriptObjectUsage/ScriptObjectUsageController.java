package net.micropact.aea.du.page.scriptObjectUsage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataEventListenerService;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DisplayMappingService;
import com.entellitrak.configuration.ETPService;
import com.entellitrak.configuration.ETPTransitionType;
import com.entellitrak.configuration.FilterHandlerService;
import com.entellitrak.configuration.FormService;
import com.entellitrak.configuration.LookupDefinitionService;
import com.entellitrak.configuration.Script;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.job.CustomJob;
import com.entellitrak.job.JobService;
import com.entellitrak.job.JobType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.PageService;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.system.SystemEventListenerService;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.map.MapBuilder;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.page.unusedScriptObjects.UnusedScriptObjectsController;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

@HandlerScript(type = PageController.class)
public class ScriptObjectUsageController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(response);

		final List<Reference> allReferences = getAllReferences(etk);

		final Map<String, List<Reference>> referencesAsMap = allReferences
				.stream()
				.collect(Collectors.toMap(
						reference -> reference.getScript().getFullyQualifiedName(),
						reference -> {
							final ArrayList<Reference> arrayList = new ArrayList<>();
							arrayList.add(reference);
							return arrayList;
						},
						(existingReferences, newReference) -> {
							existingReferences.addAll(newReference);
							return existingReferences;
						}));

		final List<Map<String, Object>> outputList = referencesAsMap.entrySet()
			.stream()
			.sorted(Comparator.comparing(Map.Entry::getKey))
			.map(entry -> Map.of(
					"fullyQualifiedScriptName", entry.getKey(),
					"references", entry.getValue()
					.stream()
					.sorted(Comparator.comparing(Reference::getName)
							.thenComparing(Reference::getBusinessKey))
					.map(reference -> new MapBuilder<>()
							.put("typeOfReference", reference.getTypeOfReference())
							.put("name", reference.getName())
							.put("businessKey", reference.getBusinessKey())
							.build())
					.collect(Collectors.toList())))
			.collect(Collectors.toList());

		response.put("scriptObjects", new Gson().toJson(outputList));

		return response;
	}

	/**
	 * This method produces a list of all references.
	 * It is public because it is used by {@link UnusedScriptObjectsController}.
	 * That page should be able to fairly easily handle any refactoring that we need to do to this
	 * page since it only cares about the script object names.
	 *
	 * @param etk entellitrak execution context
	 * @return the list of all references
	 */
	public static List<Reference> getAllReferences(final ExecutionContext etk) {
		return Arrays.stream(TypeOfReference.values())
				.flatMap(typeOfReference -> typeOfReference.getReferences(etk).stream())
				.collect(Collectors.toList());
	}

	private static void setBreadcrumbAndTitle(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Script Object Usage",
								"page.request.do?page=du.page.scriptObjectUsage")));
	}

	public enum TypeOfReference {
		PAGE_CONTROLLER(TypeOfReference::getPageControllerReferences),
		PAGE_VIEW(TypeOfReference::getPageViewReferences),
		DISPLAY_MAPPING(TypeOfReference::getDisplayMappingReferences),
		SYSTEM_EVENT_LISTENER(TypeOfReference::getSystemEventListenerReferences),
		FORM_EVENT(TypeOfReference::getFormEventReferences),
		FORM_ELEMENT_EVENT(TypeOfReference::getFormElementEventReferences),
		JOB(TypeOfReference::getJobReferences),
		LOOKUP(TypeOfReference::getLookupReferences),
		TRANSITION(TypeOfReference::getTransitionReferences),
		DATA_EVENT(TypeOfReference::getDataEventReferences),
		// TODO: Update this when core provides an API
		RESPONSIVE_SCRIPT(null),
		FILTER_HANDLER(TypeOfReference::getFilterHandlerReferences),
		// TODO: Update this when core provides an API
		SCREEN_INCLUDE(null),
		// TODO: Update this when core provides an API
		MAIN_MENU(null),
		// TODO: Update this when core provides an API
		APP_LAUNCHER(null);

		private final Function<ExecutionContext, Collection<Reference>> referenceFetcher;

		private TypeOfReference(final Function<ExecutionContext, Collection<Reference>> theReferenceFetcher) {
			referenceFetcher = theReferenceFetcher;
		}

		public Collection<Reference> getReferences(final ExecutionContext etk) {
			if(referenceFetcher == null) {
				// TODO: Remove this branch once core has APIs to support the missing types
				return List.of();
			} else {
				return referenceFetcher.apply(etk);
			}
		}

		private static Collection<Reference> getPageControllerReferences(final ExecutionContext etk){
			final PageService pageService = etk.getPageService();

			return pageService.getPages()
					.stream()
					.map(page -> {
						final Script script = pageService.getControllerScript(page);
						return new Reference(PAGE_CONTROLLER, script, page.getName(), page.getBusinessKey());
					}).collect(Collectors.toList());
		}

		private static Collection<Reference> getPageViewReferences(final ExecutionContext etk){
			final PageService pageService = etk.getPageService();

			return pageService.getPages()
					.stream()
					.map(page -> {
						final Script script = pageService.getViewScript(page);
						return new Reference(PAGE_VIEW, script, page.getName(), page.getBusinessKey());
					}).collect(Collectors.toList());
		}

		private static Collection<Reference> getDisplayMappingReferences(final ExecutionContext etk){
			final DataObjectService dataObjectService = etk.getDataObjectService();
			final DisplayMappingService displayMappingService = etk.getDisplayMappingService();

			return dataObjectService.getDataObjects()
					.stream()
					.flatMap(dataObject -> displayMappingService.getDisplayMappings(dataObject)
							.stream()
							.flatMap(displayMapping -> Optional.ofNullable(displayMappingService.getEvaluationScript(displayMapping))
									.map(script -> new Reference(
											DISPLAY_MAPPING,
											script,
											String.format("%s - %s",
													dataObject.getObjectName(),
													displayMapping.getName()),
											displayMapping.getBusinessKey()))
									.stream()))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getSystemEventListenerReferences(final ExecutionContext etk){
			final SystemEventListenerService systemEventListenerService = etk.getSystemEventListenerService();

			return systemEventListenerService.getSystemEventListeners()
					.stream()
					.map(systemEventListener -> new Reference(SYSTEM_EVENT_LISTENER, systemEventListenerService.getScript(systemEventListener), systemEventListener.getName(), systemEventListener.getBusinessKey()))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getFormEventReferences(final ExecutionContext etk){
			final FormService formService = etk.getFormService();
			final DataObjectService dataObjectService = etk.getDataObjectService();

			return dataObjectService.getDataObjects()
					.stream()
					.flatMap(dataObject -> formService.getForms(dataObject)
							.stream()
							.flatMap(form -> formService.getFormEventHandlers(form)
									.stream()
									.map(formEventHandler -> new Reference(FORM_EVENT, formService.getScript(formEventHandler), String.format("%s - %s - %s - %s",
											dataObject.getObjectName(),
											form.getName(),
											formEventHandler.getActionType(),
											formEventHandler.getEventType()
											), formEventHandler.getBusinessKey()))))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getFormElementEventReferences(final ExecutionContext etk){
			final FormService formService = etk.getFormService();
			final DataObjectService dataObjectService = etk.getDataObjectService();

			return dataObjectService.getDataObjects()
					.stream()
					.flatMap(dataObject -> formService.getForms(dataObject)
							.stream()
							.flatMap(form -> formService.getFormControls(form)
									.stream()
									.flatMap(formControl ->
									formService.getFormControlEventHandlers(formControl)
									.stream()
									.map(formControlEventHandler -> new Reference(FORM_ELEMENT_EVENT, formService.getScript(formControlEventHandler), String.format("%s - %s - %s - %s - %s",
											dataObject.getObjectName(),
											form.getName(),
											formControl.getName(),
											formControlEventHandler.getActionType(),
											formControlEventHandler.getEventType()
											), formControlEventHandler.getBusinessKey())))))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getJobReferences(final ExecutionContext etk){
			final JobService jobService = etk.getJobService();

			return jobService.getJobs()
					.stream()
					.filter(job -> Objects.equals(JobType.CUSTOM, job.getJobType()))
					.map(job -> {
						final CustomJob customJob = jobService.getCustomJobByBusinessKey(job.getBusinessKey());

						return new Reference(JOB, jobService.getScript(customJob), job.getName(), job.getBusinessKey());
					})
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getLookupReferences(final ExecutionContext etk){
			final LookupDefinitionService lookupDefinitionService = etk.getLookupDefinitionService();

			return lookupDefinitionService.getLookupDefinitions()
					.stream()
					.flatMap(lookupDefinition -> {
						final Optional<Script> optionalScript;

						switch(lookupDefinition.getSourceType()) {
						case DATA_OBJECT:
							optionalScript = Optional.ofNullable(lookupDefinitionService.getDataObjectLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey())
									.getFilterScript());
							break;
						case LIST_BASED_SCRIPT:
							optionalScript = Optional.of(lookupDefinitionService.getListBasedScriptLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey()).getScript());
							break;
						case SCRIPT:
							optionalScript = Optional.of(lookupDefinitionService.getScriptLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey()).getScript());
							break;
						case SQL_QUERY:
							optionalScript = Optional.of(lookupDefinitionService.getSqlScriptLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey()).getScript());
							break;
						case SYSTEM_OBJECT:
							optionalScript = Optional.empty();
							break;
						default:
							throw new GeneralRuntimeException(String.format("Lookup Type not yet supported %s", lookupDefinition.getSourceType()));
						}

						return optionalScript
								.map(script -> new Reference(LOOKUP, script, lookupDefinition.getName(), lookupDefinition.getBusinessKey()))
								.stream();

					})
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getTransitionReferences(final ExecutionContext etk){
			final ETPService etpService = etk.getETPService();
			final DataObjectService dataObjectService = etk.getDataObjectService();

			return dataObjectService.getBaseTrackedObjects()
					.stream()
					.flatMap(dataObject -> etpService.getWorkflow(dataObject)
							.getStates()
							.stream()
							.flatMap(state -> state.getTransitionsFrom()
									.stream()
									.filter(transition -> Objects.equals(ETPTransitionType.BUSINESS_LOGIC, transition.getTransitionType()))
									.flatMap(transition -> Optional.ofNullable(etpService.getTriggerScript(transition))
											.map(script -> new Reference(
													TRANSITION,
													script,
													String.format("%s - %s - %s",
															dataObject.getObjectName(),
															state.getName(),
															transition.getName()),
													transition.getBusinessKey()))
											.stream())))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getDataEventReferences(final ExecutionContext etk){
			final DataEventListenerService dataEventListenerService = etk.getDataEventListenerService();
			final DataObjectService dataObjectService = etk.getDataObjectService();

			return dataObjectService.getDataObjects()
					.stream()
					.flatMap(dataObject
							-> dataEventListenerService.getDataEventListeners(dataObject)
							.stream()
							.map(dataEventListener
									-> new Reference(
											DATA_EVENT,
											dataEventListener.getScript(),
											String.format("%s - %s",
													dataObject.getObjectName(),
													dataEventListener.getEventType())
											, dataEventListener.getBusinessKey())))
					.collect(Collectors.toList());
		}

		private static Collection<Reference> getFilterHandlerReferences(final ExecutionContext etk){
			final DataObjectService dataObjectService = etk.getDataObjectService();
			final FilterHandlerService filterHandlerService = etk.getFilterHandlerService();

			return dataObjectService.getDataObjects()
					.stream()
					.flatMap(dataObject
							-> filterHandlerService.getFilterHandlers(dataObject)
							.stream()
							.map(filterHandler -> new Reference(FILTER_HANDLER, filterHandler.getScript(), String.format("%s - %s",
									dataObject.getObjectName(),
									filterHandler.getType()),
									filterHandler.getBusinessKey())))
					.collect(Collectors.toList());
		}
	}

	public static class Reference {
		TypeOfReference typeOfReference;
		Script script;
		String name;
		String businessKey;

		Reference(final TypeOfReference theTypeOfReference, final Script theScript, final String theName, final String theBusinessKey){
			typeOfReference = theTypeOfReference;
			script = theScript;
			name = theName;
			businessKey = theBusinessKey;
		}

		public TypeOfReference getTypeOfReference() {
			return typeOfReference;
		}

		public Script getScript() {
			return script;
		}

		public String getName() {
			return name;
		}

		public String getBusinessKey() {
			return businessKey;
		}
	}
}