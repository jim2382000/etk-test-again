package net.entellitrak.aea.gl.api.etk;

import java.util.Objects;

import com.entellitrak.BaseObjectEventContext;
import com.entellitrak.configuration.Form;
import com.entellitrak.configuration.FormControl;
import com.entellitrak.configuration.FormService;

/**
 * Utility class for dealing with {@link FormControl}.
 *
 * @author Zachary.Miller
 */
public final class FormControlUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private FormControlUtil() {
    }

    /**
     * Find a form control with a given name on the current form during a {@link BaseObjectEventContext}.
     *
     * @param etk entellitrak execution context
     * @param formControlName the form control name
     * @return the form control, or null if it was not found
     */
    public static FormControl findFormControlForBaseObjectEventContext(final BaseObjectEventContext etk, final String formControlName) {
        final FormService formService = etk.getFormService();

        final Form form = FormUtil.getFormForBaseObjectEventContext(etk);

        return formService.getFormControls(form)
                .stream()
                .filter(formControl -> Objects.equals(formControlName, formControl.getName()))
                .findAny()
                .orElse(null);
    }
}
