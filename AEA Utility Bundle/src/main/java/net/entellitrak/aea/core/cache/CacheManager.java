package net.entellitrak.aea.core.cache;

import java.io.Serializable;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.cache.Cache;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class contains the code which is common to anything which needs to store something in the {@link Cache}.
 * It is recommended to use this class instead of accessing the cache directly.
 *
 * @author zmiller
 * @see AClassKeyCacheable
 * @see ICacheable
 */
public final class CacheManager {

	/**
	 * Utility classes do not need public constructors.
	 */
	private CacheManager(){}

	/**
	 * This method will load the value represented by the cacheable from the cache if it is there, otherwise will
	 * put the value into the cache and return it.
	 *
	 * @param <T> The type of the object being cached
	 * @param etk entellitrak execution context
	 * @param cacheable A specification of what should be stored in the cache
	 * @return The value of the cacheable
	 */
	public static <T extends Serializable> T loadSerializable(final ExecutionContext etk, final ICacheable<T> cacheable) {
		final Cache<String, Serializable> cache = etk.getSerializableCache();

		final String key = cacheable.getKey();

		@SuppressWarnings("unchecked")
		T value = (T) cache.load(key);

		if(value == null){
			try {
				value = cacheable.getValue();
			} catch (final ApplicationException e) {
				throw new GeneralRuntimeException(String.format("Problem attempting to load value from cache under key \"%s\"", key),
						e);
			}
			if(value == null){
				throw new GeneralRuntimeException(String.format("Attempted to store a null value in the cache under key \"%s\". You should never attempt to store a null value in the cache.",
						key));
			}else{
				cache.store(key, value);
			}
		}

		return value;
	}

	/**
	 * This method will remove a single item from the cache.
	 * <em>
	 *  Note that this leaves open the possibility of race conditions if you rely on this functionality in a
	 *  production environment since other transactions may put the old value back into the cache before your current
	 *  transaction is committed.
	 * </em>
	 *
	 * @param <T> The type of the object being cached
	 * @param etk entellitrak execution context
	 * @param cacheable A specification of the object which is to be removed from the cache
	 */
	public static <T> void removeSerializable(final ExecutionContext etk, final ICacheable<T> cacheable){
		final Cache<String, Serializable> cache = etk.getSerializableCache();

		cache.remove(cacheable.getKey());
	}

	/**
	 * This method will load the value represented by the cacheable from the cache if it is there, otherwise will
	 * put the value into the cache and return it.
	 *
	 * @param <T> The type of the object being cached
	 * @param etk entellitrak execution context
	 * @param cacheable A specification of what should be stored in the cache
	 * @return The value of the cacheable
	 * @throws ApplicationException If there was an underlying {@link ApplicationException}
	 *
	 * @deprecated Use {@link #loadSerializable(ExecutionContext, ICacheable)}
	 */
	@Deprecated(forRemoval = true)
	public static <T> T load(final ExecutionContext etk, final ICacheable<T> cacheable) throws ApplicationException{
		final Cache<String, Object> cache = etk.getCache();

		final String key = cacheable.getKey();

		@SuppressWarnings("unchecked")
		T value = (T) cache.load(key);

		if(value == null){
			value = cacheable.getValue();
			if(value == null){
				throw new GeneralRuntimeException(String.format("Attempted to store a null value in the cache under key \"%s\". You should never attempt to store a null value in the cache.",
						key));
			}else{
				cache.store(key, value);
			}
		}

		return value;
	}

	/**
	 * This method will remove a single item from the cache.
	 * <em>
	 *  Note that this leaves open the possibility of race conditions if you rely on this functionality in a
	 *  production environment since other transactions may put the old value back into the cache before your current
	 *  transaction is committed.
	 * </em>
	 *
	 * @param <T> The type of the object being cached
	 * @param etk entellitrak execution context
	 * @param cacheable A specification of the object which is to be removed from the cache
	 *
	 * @deprecated Use {@link #removeSerializable(ExecutionContext, ICacheable)}
	 */
	@Deprecated(forRemoval = true)
	public static <T> void remove(final ExecutionContext etk, final ICacheable<T> cacheable){
		final Cache<String, Object> cache = etk.getCache();

		cache.remove(cacheable.getKey());
	}
}
