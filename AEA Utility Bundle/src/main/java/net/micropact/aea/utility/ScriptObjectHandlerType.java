package net.micropact.aea.utility;

import java.util.stream.Stream;

/**
 * Enum containing the Script Object Handler Types that entellitrak uses.
 * The values can be found in com.micropact.entellitrak.workspace.model.HandlerType
 *
 * @author zmiller
 */
public enum ScriptObjectHandlerType {

    /** NONE. */
    NONE(0, "None"),
    /** ADV_SEARCH_PROCESSOR. */
    ADV_SEARCH_PROCESSOR(1, "AdvancedSearchEventHandler"),
    /** DISPLAY_MAPPING_HANDLER. */
    DISPLAY_MAPPING_HANDLER(2, "DisplayMappingHandler"),
    /** FORM_ELEMENT_EVENT_HANDLER. */
    FORM_ELEMENT_EVENT_HANDLER(3, "FormElementEventHandler"),
    /** FORM_EVENT_HANDLER. */
    FORM_EVENT_HANDLER(4, "FormEventHandler"),
    /** JOB_HANDLER. */
    JOB_HANDLER(5, "JobHandler"),
    /** LOOKUP_HANDLER. */
    LOOKUP_HANDLER(6, "LookupHandler"),
    /** PAGE_CONTROLLER. */
    PAGE_CONTROLLER(7, "PageController"),
    /** TRANSITION_HANDLER. */
    TRANSITION_HANDLER(8, "TransitionHandler"),
    /** USER_EVENT_HANDLER. */
    USER_EVENT_HANDLER(9, "UserEventHandler"),
    /** SCAN_EVENT_HANDLER. */
    SCAN_EVENT_HANDLER(10, "ScanEventHandler"),
    /** STEP_BASED_PAGE. */
    STEP_BASED_PAGE(11, "StepBasedPageHandler"),
    /** FORM_EXECUTION_HANDLER. */
    FORM_EXECUTION_HANDLER(12, "FormExecutionHandler"),
    /** DATA_OBJECT_EVENT_HANDLER. */
    DATA_OBJECT_EVENT_HANDLER(13, "DataObjectEventHandler"),
    /** APPLY_CHANGES_EVENT_HANDLER. */
    APPLY_CHANGES_EVENT_HANDLER(14, "ApplyChangesEventHandler"),
    /** CHANGE_HANDLER. */
    CHANGE_HANDLER(15, "ChangeHandler"),
    /** CLICK_HANDLER. */
    CLICK_HANDLER(16, "ClickHandler"),
    /** NEW_HANDLER. */
    NEW_HANDLER(17, "NewHandler"),
    /** READ_HANDLER. */
    READ_HANDLER(18, "ReadHandler"),
    /** SAVE_HANDLER. */
    SAVE_HANDLER(19, "SaveHandler"),
    /** OFFLINE_SYNC_HANDLER. */
    OFFLINE_SYNC_HANDLER(20, "OfflineSyncHandler"),
    /** REFERENCE_OBJECT_EVENT_HANDLER. */
    REFERENCE_OBJECT_EVENT_HANDLER(21, "ReferenceObjectEventHandler"),
    /** ENDPOINT_HANDLER. */
    ENDPOINT_HANDLER(22, "EndpointHandler"),
    /** DEPLOYMENT_HANDLER. */
    DEPLOYMENT_HANDLER(23, "DeploymentHandler"),
    /** ELEMENT_FILTER_HANDLER. */
    ELEMENT_FILTER_HANDLER(24, "ElementFilterHandler"),
    /** RECORD_FILTER_HANDLER. */
    RECORD_FILTER_HANDLER(25, "RecordFilterHandler"),
    /** LIST_BASED_LOOKUP_HANDLER. */
    LIST_BASED_LOOKUP_HANDLER(26, "ListBasedLookupHandler"),
    /** MENU_CONTROLLER */
    MENU_CONTROLLER(27, "MenuController"),
    /** CHECK_IN_COMPLETE_EVENT_HANDLER */
    CHECK_IN_COMPLETE_EVENT_HANDLER(28, "CheckInEventHandler"),
    /** LOGIN_EXTENSION_HANDLER */
    LOGIN_EXTENSION_HANDLER(29, "LoginExtensionHandler"),
    /** APP_LAUNCHER_CONTROLLER */
    APP_LAUNCHER_CONTROLLER(30, "AppLauncherController");

    /**
     * The id which entellitrak internally uses to represent the Script Object Handler.
     */
    private final long id;
    /**
     * The name entellitrak uses to represent the Script Object Handler.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param scriptObjectHandlerTypeId The id which entellitrak internally uses to represent the
     *          Script Object Handler.
     * @param scriptObjectHandlerTypeName The name entellitrak uses to represent the Script Object Handler.
     */
    ScriptObjectHandlerType(final long scriptObjectHandlerTypeId, final String scriptObjectHandlerTypeName){
        id = scriptObjectHandlerTypeId;
        name = scriptObjectHandlerTypeName;
    }

    /**
     * Get the number entellitrak uses internally to represent the Script Object Handler.
     *
     * @return The id entellitrak internally uses to represent the Script Object Handler.
     */
    public long getId(){
        return id;
    }

    /**
     * Get the name entellitrak uses to represent the Script Object Handler.
     *
     * @return The name entellitrak uses to represent the Script Object Handler.
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the ScriptObjectHandler given the id that entellitrak uses to reference it.
     *
     * @param id The id entellitrak internally uses to represent the Script Object Handler.
     * @return The ScriptObjectHandlerType corresponding to the given internal entellitrak id.
     */
    public static ScriptObjectHandlerType getById(final long id){
        return Stream.of(values())
                .filter(handlerType -> id == handlerType.getId())
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find ScriptObjectHandlerType for id: %s", id)));
    }
}
