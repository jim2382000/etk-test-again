package net.micropact.aea.core.cache;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.logging.Logger;

import net.entellitrak.aea.core.cache.AClassKeyCacheable;
import net.micropact.aea.core.enums.AeaCoreConfigurationItem;
import net.micropact.aea.utility.DynamicObjectUtility;

/**
 * This class is an implementation of {@link net.entellitrak.aea.core.cache.ICacheable} for storing the values of
 * AEA_CORE_CONFIGURATION.
 *
 * @author zachary.miller
 */
public class AeaCoreConfigurationCacheable extends AClassKeyCacheable<HashMap<String, Object>> {

	private final ExecutionContext etk;

	/**
	 * Constructor for AeaConfigurationCacheable.
	 *
	 * @param executionContext
	 *            entellitrak execution context
	 */
	public AeaCoreConfigurationCacheable(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public HashMap<String, Object> getValue() throws ApplicationException {
		final Logger logger = etk.getLogger();

		final HashMap<String, Object> map = new HashMap<>();

		Stream.of(AeaCoreConfigurationItem.values())
		.filter(AeaCoreConfigurationItem::isCacheable)
		.forEach(configurationItem -> {
			final String value = DynamicObjectUtility.getPropertyFromObjectWithBusinessKey(etk,
					"object.aeaCoreConfiguration.element.code",
					configurationItem.getCode(),
					"value");

			if (Objects.isNull(value)) {
				/*
				 * At the moment, this cannot use Utility.aeaLog because it gets a StackOverflowError because
				 * when an item doesn't exist, it tries to write to the log, which tries to reload all the
				 * items, which tries to write to the log, etc.
				 */
				logger.error(
						String.format(
								"Did not find value in AEA Core Configuration RDO with code \"%s\". The value is being defaulted to null but you should configure the desired value.",
								configurationItem.getCode()));
			}

			map.put(configurationItem.getCode(), configurationItem.getDeserializer().deserialize(value));
		});

		return map;
	}
}
