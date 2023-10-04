package net.micropact.aea.core.dataTypePlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.AbstractDataElementType;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataElementType;
import com.entellitrak.configuration.FormControl;
import com.entellitrak.configuration.FormControlModel;
import com.entellitrak.configuration.FormService;
import com.entellitrak.configuration.Plugin;
import com.entellitrak.configuration.StringValues;
import com.entellitrak.configuration.ValuesFactory;
import com.entellitrak.form.FormControlData;
import com.micropact.entellitrak.web.HandlebarsFacade;
import com.micropact.entellitrak.web.taglib.FormControlDataImpl;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 *
 * Methods for utilizing the class associated with data elements that use Data Type Plugins.
 *
 * @author ahargrave
 **/
public final class DataTypePluginClassUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private DataTypePluginClassUtility(){}

	/**
	 * Creates an instance of the main Data Type Plugin class
	 * associated with the Data Element with the given data
	 * element business key.
	 *
	 * @param etk entellitrak execution context
	 * @param dataElementBusinessKey business key of the data element
	 * @return The class which contains the data-type plugin's implementation
	 * @throws ApplicationException If there was an underlying exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataElementType instantiatePluginClass(final ExecutionContext etk, final String dataElementBusinessKey) throws ApplicationException{
	    final DataElementService dataElementService = etk.getDataElementService();
	    final com.entellitrak.configuration.DataElement dataElement =
	            dataElementService.getDataElementByBusinessKey(dataElementBusinessKey);

		try {
			return ((Class<DataElementType>) Class.forName(dataElement.getPlugin().getClassName())).getDeclaredConstructor().newInstance();
		} catch (final InstantiationException | IllegalAccessException | ClassNotFoundException
		        | IllegalArgumentException | InvocationTargetException
		        | NoSuchMethodException | SecurityException e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Converts the given {@link String} value to whatever class is
	 * needed by the Data Type Plugin main class associated to the
	 * Data Element Business Key.  The converted value is then passed
	 * through getViewString of the Data Type Plugin main class to
	 * produce the proper display value.
	 *
	 * @param etk entellitrak execution context
	 * @param dataElementBusinessKey The business key of the data element
	 * @param value the raw value of the data element
	 * @return The display string which would be used in the view.
	 * @throws ApplicationException If there was an underlying Exception
	 */
	public static String getDataTypePluginDisplayFromStringValue(final ExecutionContext etk, final String dataElementBusinessKey, final String value) throws ApplicationException{
		final DataElementType mainPluginClassObject = DataTypePluginClassUtility.instantiatePluginClass(etk, dataElementBusinessKey);
		final Object finalValue = mainPluginClassObject.buildValueObject(
			new StringValues(){
				@Override
				public Set<String> getKeys() {
					return Collections.emptySet();
				}
				@Override
				public String getValue() {
					return value;
				}
				@Override
				public String getValue(final String key) {
					return value;
				}
				@Override
				public List<String> getValues() {
					return Arrays.asList(value);
				}
				@Override
				public List<String> getValues(final String key) {
					return Arrays.asList(value);
				}
			}
		);

		return mainPluginClassObject.getViewString(finalValue);
    }

	/**
	 * **BETA** Method to return a Custom Data Type's HTML widget. Relies heavily on the private APIs.
	 *
	 * @param etk entellitrak execution context
	 * @param dataElementBusinessKey business key of the data element
	 * @param formControlName The name of the form control (including the underscore. ie: Person_SSN)
	 * @param formControlBusinessKey A Form Control business key which has the custom data type bound to it
	 * @param currentValue The current value of the data element
	 * @param getJS flag indicating whether this method should include the custom js
	 * @return The HTML representation of the data type plugin
	 * @throws IOException If there was an underlying {@link IOException}
	 */
	public static String getPluginDataTypeWidget(final ExecutionContext etk,
			final String dataElementBusinessKey,
			final String formControlName,
			final String formControlBusinessKey,
			final String currentValue,
			final boolean getJS) throws IOException {
	    final DataElementService dataElementService = etk.getDataElementService();

		final DataElement dataElement = dataElementService.getDataElementByBusinessKey(dataElementBusinessKey);

		if (dataElement.getPlugin() != null) {
		    final Plugin plugin = dataElement.getPlugin();
		    AbstractDataElementType<?> pluginImplementation;
            try {
                pluginImplementation = (AbstractDataElementType<?>) Class.forName(plugin.getClassName()).getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new GeneralRuntimeException(e);
            }

			final FormControlModel formControlModel = pluginImplementation.getFormControlModel();

			String code = null;

			if (getJS) {
				code = formControlModel.getJsIncludeScript();
			} else {
				if (etk.getCurrentUser().getProfile().isAccessibilityEnhanced()) {
					code = formControlModel.getAccessibleEditableControlScript();
				} else {
					code = formControlModel.getEditableControlScript();
				}
			}

			final FormControlData formControlData = convertRuntimeControlToControlData(formControlBusinessKey, etk);
			formControlData.setValues(ValuesFactory.getStringValues(currentValue));
			formControlData.setHelper(pluginImplementation
					.getFormControlHelper(ValuesFactory.getStringValues(currentValue)));
			formControlData.setControlName(formControlName);

			final HandlebarsFacade facade = new HandlebarsFacade();
			return facade.transformTemplate(code, formControlData);
		}

		return "";
	}

	private static FormControlData convertRuntimeControlToControlData(final String formControlBusinessKey,
																      final ExecutionContext etk) {
	    final FormService formService = etk.getFormService();

        final FormControlData formControlData = new FormControlDataImpl();

        final FormControl formControl = formService.getFormControl(formControlBusinessKey);

        if (formControl.getHeight() != null) {
            formControlData.addAttribute("height", formControl.getHeight().toString());
        }
        if (formControl.getWidth() != null) {
            formControlData.addAttribute("width", formControl.getWidth().toString());
        }
        if (formControl.getTooltipText() != null) {
            formControlData.addAttribute("title", formControl.getTooltipText());
        }

        formControlData.addAttribute("x", "0");
        formControlData.addAttribute("y", "0");
        formControlData.addAttribute("required", false);
        formControlData.addAttribute("update", false);

        return formControlData;
    }
}
