package net.micropact.aea.core.pageUtility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.page.Breadcrumb;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.utility.DynamicObjectInstanceUtils;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.core.utility.StringEscapeUtils;

/**
 * Utility class for dealing with core page breadcrumb.
 *
 * @author Zachary.Miller
 */
public final class BreadcrumbUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private BreadcrumbUtility() {
    }

    /**
     * Get the last child of a breadcrumb.
     *
     * @param breadcrumb the breadcrumb
     * @return the last child
     */
    private static Breadcrumb getLastChild(final Breadcrumb breadcrumb) {
        return Optional.ofNullable(breadcrumb.getChild())
            .map(BreadcrumbUtility::getLastChild)
            .orElse(breadcrumb);
    }

    /**
     * Set the breadcrumb and title for a text response based on a simple breadcrumb.
     * The title will be the title of the simple breadcrumb, that is the 'last' breadcrumb.
     * <strong>This should only be used when the title does not have HTML.</strong>
     *
     * @param response the text response
     * @param breadcrumb the breadcrumb
     */
    public static void setBreadcrumbAndTitle(final TextResponse response, final Breadcrumb breadcrumb) {
        response.setBreadcrumb(breadcrumb);

        // FIXME: Figure out if we want to do something else. Breadcrumb titles are HTML, Page titles are not.
        response.setTitle(getLastChild(breadcrumb).getTitle());
    }

    /**
     * Get the tracing inbox breadcrumb for a particular data object.
     *
     * @param dataObject the data object
     * @return Get the tracking inbox breadcrumb
     */
    public static Breadcrumb getTrackingInboxBreadcrumbForObject(final DataObject dataObject) {
        try {
            return new SimpleBreadcrumb("Tracking Inbox",
                    String.format("tracking.dashBoard.do?dataObjectKey=%s",
                            URLEncoder.encode(dataObject.getBusinessKey(), StandardCharsets.UTF_8.name())));
        } catch (final UnsupportedEncodingException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Generate a breadcrumb for a data object listing screen.
     * This includes all ancestors of the listing all the way up to, and including the tracking inbox.
     *
     * @param etk entellitrak execution context
     * @param parentObject the parent object
     * @param childObject the child object
     * @return the breadcrumb
     */
    public static Breadcrumb getBreadcrumbForListing(final ExecutionContext etk, final DataObjectInstance parentObject, final DataObject childObject) {
        try {
            final String title = StringEscapeUtils.escapeHtml(String.format("%s Listing", childObject.getLabel()));

            final String childBusinessKey = childObject.getBusinessKey();
            final String url = String.format("workflow.list.do?dataObjectKey=%s&trackingId=%s",
                    URLEncoder.encode(childBusinessKey, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(String.valueOf(parentObject.properties().getId()), StandardCharsets.UTF_8.name()));

            final Breadcrumb parentBreadcrumb = getBreadcrumbForDataObjectInstance(etk, parentObject);

            return addLastChildFluent(parentBreadcrumb, new SimpleBreadcrumb(title, url));
        } catch (final UnsupportedEncodingException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get a breadcrumb for a particular data object instance.
     * This includes all ancestors of the data object all the way up to, and including the tracking inbox.
     *
     * @param etk entellitrak execution context
     * @param dataObjectInstance the data object instance
     * @return the breadcrumb
     */
    public static Breadcrumb getBreadcrumbForDataObjectInstance(final ExecutionContext etk, final DataObjectInstance dataObjectInstance) {
        final String url = getDataObjectInstanceUrl(dataObjectInstance);

        final String title = getBreadcrumbTitle(etk, dataObjectInstance);

        final Breadcrumb parentBreadcrumb;

        final DataObject dataObject = dataObjectInstance.configuration();

        if(EtkDataUtils.isRootDataObject(etk, dataObject)) {
            parentBreadcrumb = BreadcrumbUtility.getTrackingInboxBreadcrumbForObject(dataObject);
        }else {
            parentBreadcrumb = BreadcrumbUtility.getBreadcrumbForListing(etk, DynamicObjectInstanceUtils.getParentDataObjectInstance(etk, dataObjectInstance), dataObject);
        }

        return addLastChildFluent(parentBreadcrumb, new SimpleBreadcrumb(title, url));
    }

    /**
     * Get the title of a breadcrumb for a data object instance.
     * This includes the data object label and identifier.
     *
     * @param etk entellitrak execution context
     * @param dataObjectInstance the data object instance
     * @return the breadcrumb title
     */
    private static String getBreadcrumbTitle(final ExecutionContext etk, final DataObjectInstance dataObjectInstance) {
        final String label = dataObjectInstance.configuration().getLabel();

        return String.format("%s%s",
                StringEscapeUtils.escapeHtml(label),
                getBreadcrumbIdentifierFragment(etk, dataObjectInstance));
    }

    /**
     * Get the HTML to use in a breadcrumb for the identifier of a particular data object instance.
     *
     * @param etk entellitrak execution context
     * @param dataObjectInstance the data object instance
     * @return the breadcrumb identifier fragment
     */
    private static String getBreadcrumbIdentifierFragment(final ExecutionContext etk, final DataObjectInstance dataObjectInstance) {
        return etk.getDataElementService().getDataElements(dataObjectInstance.configuration())
                .stream()
                .filter(DataElement::isIdentifier)
                .findAny()
                .map(dataElement -> dataObjectInstance.get(Object.class, dataElement.getPropertyName()))
                .map(value -> String.format(" (<span class=\"etk-path-identifier\">%s</span>)",
                        StringEscapeUtils.escapeHtml(value.toString())))
                .orElse("");
    }

    /**
     * Get the URL which would be used to access a particular data object instance.
     *
     * @param dataObjectInstance the data object instance
     * @return the URL to access the data object
     */
    private static String getDataObjectInstanceUrl(final DataObjectInstance dataObjectInstance) {
        try {
            final String businessKey = dataObjectInstance.configuration().getBusinessKey();
            final long trackingId = dataObjectInstance.properties().getId();

            return String.format("workflow.do?dataObjectKey=%s&trackingId=%s",
                    URLEncoder.encode(businessKey, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(String.valueOf(trackingId), StandardCharsets.UTF_8.name()));
        } catch (final UnsupportedEncodingException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Sets the last child breadcrumb of a breadcrumb and returns the parent.
     *
     * @param parentBreadcrumb the parent breadcrumb
     * @param childBreadcrumb the child breadcrumb
     * @return the parent breadcrumb
     */
    public static Breadcrumb addLastChildFluent(final Breadcrumb parentBreadcrumb, final Breadcrumb childBreadcrumb) {
        parentBreadcrumb.addLastChild(childBreadcrumb);
        return parentBreadcrumb;
    }
}
