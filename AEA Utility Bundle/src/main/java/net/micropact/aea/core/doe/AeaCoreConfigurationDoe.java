package net.micropact.aea.core.doe;

import com.entellitrak.InputValidationException;
import com.entellitrak.ReferenceObjectEventContext;
import com.entellitrak.dynamic.AeaCoreConfiguration;
import com.entellitrak.dynamic.DynamicObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.legacy.workflow.WorkflowResult;
import com.entellitrak.tracking.ReferenceObjectEventHandler;

import net.entellitrak.aea.core.cache.CacheManager;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.cache.AeaCoreConfigurationCacheable;
import net.micropact.aea.core.utility.StringUtils;

/**
 * Data Object Event Handler for the AEA Core Configuration object.
 *
 * @author zachary.miller
 */
@HandlerScript(type = ReferenceObjectEventHandler.class)
public class AeaCoreConfigurationDoe extends AReferenceObjectEventHandler {

	@Override
	protected void executeObject(final ReferenceObjectEventContext etk) {
		convertDescriptionToUnixLineEndings(etk);

		clearRelevantCache(etk);
	}

	/**
	 * This method converts the line-endings of the description to unix-style.
	 * The reason is to make them consistent for version-control of our export files.
	 *
	 * @param etk entellitrak execution context
	 */
	private static void convertDescriptionToUnixLineEndings(final ReferenceObjectEventContext etk) {
		try {
			final DynamicObjectService dynamicObjectService = etk.getDynamicObjectService();

			final AeaCoreConfiguration aeaCoreConfiguration = dynamicObjectService.get(AeaCoreConfiguration.class, etk.getNewObject().properties().getId());

			aeaCoreConfiguration.setDescription(StringUtils.toUnixLineEndings(aeaCoreConfiguration.getDescription()));

			dynamicObjectService.createSaveOperation(aeaCoreConfiguration)
			.setExecuteEvents(false)
			.save();
		} catch (final InputValidationException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	/**
	 * Clears the caches which may have been invalidated by saving of the AEA Core Configuration object.
	 *
	 * @param etk entellitrak execution context.
	 */
	private static void clearRelevantCache(final ReferenceObjectEventContext etk) {
		final WorkflowResult result = etk.getResult();

		result.addMessage("The values within the AEA CORE Configuration reference table are cached. Although the cache has just been cleared it is possible that another user has loaded old values back into the cache. If the site appears to continue to use the old value, you must manually clear the cache for the changes to take effect.");
		CacheManager.removeSerializable(etk, new AeaCoreConfigurationCacheable(etk));
	}
}
