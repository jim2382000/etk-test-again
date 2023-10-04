package net.entellitrak.aea.gl.api.etk;

import java.util.Objects;

import com.entellitrak.BaseObjectEventContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.Form;
import com.entellitrak.configuration.FormService;

/**
 * Utility for dealing with entellitrak forms.
 *
 * @author Zachary.Miller
 */
public final class FormUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private FormUtil() {
    }

    /**
     * Get the {@link Form} for a given {@link BaseObjectEventContext}.
     *
     * @param etk entellitrak execution context
     * @return the form, or null if one could not be found
     */
    public static Form getFormForBaseObjectEventContext(final BaseObjectEventContext etk) {
        final FormService formService = etk.getFormService();

        final String formName = etk.getForm().getName();

        final DataObject dataObject = etk.getNewObject().configuration();

        return formService.getForms(dataObject)
                .stream()
                .filter(form -> Objects.equals(formName, form.getName()))
                .findAny()
                .orElse(null);
    }
}
