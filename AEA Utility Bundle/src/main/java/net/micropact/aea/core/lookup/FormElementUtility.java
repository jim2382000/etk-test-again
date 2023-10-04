package net.micropact.aea.core.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.entellitrak.form.FormElement;

import net.micropact.aea.utility.Utility;

/**
 * Utility class for dealing with {@link FormElement}.
 *
 * @author Zachary.Miller
 */
public final class FormElementUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private FormElementUtility() {
    }

    /**
     * A uniform way to get the value/values of a form element.
     * This method is necessary because the getValue method does not work for multi-selects and the
     * getValues method only works for multiselects.
     * The returned collection will not be null, and will not contain null.
     *
     * @param formElement the form element
     * @return the value/values
     */
    public static Collection<String> getFormValues(final FormElement formElement) {
        final List<String> values = formElement.getValues();
        final String value = formElement.getValue();

        if(values != null) {
            return values;
        } else if(!Utility.isBlank(value)) {
            return Collections.singletonList(value);
        } else {
            return Collections.emptyList();
        }
    }
}
