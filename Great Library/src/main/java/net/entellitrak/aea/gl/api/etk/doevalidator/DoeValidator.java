package net.entellitrak.aea.gl.api.etk.doevalidator;

import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;

import com.entellitrak.BaseObjectEventContext;
import com.entellitrak.DataEventType;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.dynamic.DynamicObjectService;

import net.entellitrak.aea.gl.api.etk.FormControlUtil;
import net.entellitrak.aea.gl.api.etk.WorkflowResultUtil;

/**
 * Class for performing common validations within {@link BaseObjectEventContext}.
 *
 * @author Zachary.Miller
 */
public final class DoeValidator {

    /**
     * Utility classes do not need public constructors.
     */
    private DoeValidator() {
    }

    /**
     * Validate that a date on the data form is not before another date on the data form.
     * If it is, cancels the transaction and adds a message.
     *
     * @param etk entellitrak execution context
     * @param dateToValidateName the name of the date to validate
     * @param dateWhichCannotBePrecededName the name of the date which cannot be preceded
     */
    public static void validateDateIsNotBeforeAnother(final BaseObjectEventContext etk, final String dateToValidateName, final String dateWhichCannotBePrecededName) {
        final DataObjectInstance object = etk.getNewObject();

        if(EnumSet.of(DataEventType.CREATE, DataEventType.UPDATE)
                .contains(etk.getDataEventType())) {
            final Date dateToValidate = object.get(Date.class, dateToValidateName);
            final Date dateWhichCannotBePreceded = object.get(Date.class, dateWhichCannotBePrecededName);

            if(dateToValidate != null
                    && dateWhichCannotBePreceded != null
                    && dateToValidate.before(dateWhichCannotBePreceded)) {
                WorkflowResultUtil.cancelTransactionMessage(etk,
                        String.format("%s cannot be before %s.",
                                FormControlUtil.findFormControlForBaseObjectEventContext(etk, dateToValidateName).getLabel(),
                                FormControlUtil.findFormControlForBaseObjectEventContext(etk, dateWhichCannotBePrecededName).getLabel()));
            }
        }
    }

    /**
     * Validate that a data element is unique (that there is no other record that has the same value for this field).
     * If it is not, cancels the transaction and adds a message.
     *
     * @param etk entellitrak execution context
     * @param dataElement the data element
     */
    public static void validateUnique(final BaseObjectEventContext etk, final DataElement dataElement) {
        final DataObjectInstance object = etk.getNewObject();
        final DynamicObjectService dynamicObjectService = etk.getDynamicObjectService();

        if(EnumSet.of(DataEventType.CREATE, DataEventType.UPDATE)
                .contains(etk.getDataEventType())) {
            final long trackingId = object.properties().getId();

            final String propertyName = dataElement.getPropertyName();

            final String value = object.get(String.class, propertyName);

            final DataObject dataObject = dataElement.getDataObject();

            final boolean isValueAlreadyUsed = dynamicObjectService.getBySingleElementCriteria(dataElement.getBusinessKey(), value)
                    .stream()
                    .anyMatch(dynamicObject -> !Objects.equals(trackingId, dynamicObject.properties().getId()));

            if(isValueAlreadyUsed) {
                final String formControlLabel = FormControlUtil.findFormControlForBaseObjectEventContext(etk, propertyName).getLabel();

                WorkflowResultUtil.cancelTransactionMessage(etk,
                        String.format("%s must be unique. There is already another %s with a value of %s for %s",
                                formControlLabel,
                                dataObject.getLabel(),
                                value,
                                formControlLabel));
            }
        }
    }
}
