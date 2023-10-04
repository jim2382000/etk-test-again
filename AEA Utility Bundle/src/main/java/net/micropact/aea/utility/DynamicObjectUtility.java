package net.micropact.aea.utility;

import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.dynamic.DataObjectInstance;

import net.micropact.aea.core.utility.StreamUtils;

/**
 * A dynamic object service helper class.
 *
 * @author Solutions Management
 *
 */
public class DynamicObjectUtility {

    private DynamicObjectUtility() {
        throw new IllegalStateException("DynamicObjectUtility class");
    }

    public static Long findIdByCode(final ExecutionContext etk, final String businessKey, final String code) {
        return etk.getDynamicObjectService()
            .getBySingleElementCriteria(businessKey, code)
            .stream()
            .map(o -> o.properties().getId())
            .findAny()
            .orElse(null);
    }

    public static <T> T getSingletonPropertyFromObjectWithBusinessKey(final ExecutionContext etk,
        final String elementCode, final Class<T> cls, final String property, final String value) {
        return Optional.ofNullable(etk.getDynamicObjectService()
            .getBySingleElementCriteria(elementCode, value)
            .stream()
            .collect(StreamUtils.toSingletonCollector()))
            .map(o -> o.get(cls, property))
            .orElse(null);
    }

    public static String getPropertyFromObjectWithBusinessKey(final ExecutionContext etk,
        final String businessKey, final String value, final String property) {
        return etk.getDynamicObjectService()
            .getBySingleElementCriteria(businessKey, value)
            .stream()
            .findAny()
            .map(o -> o.get(String.class, property))
            .orElse(null);
    }

    public static <T extends DataObjectInstance> T getSingletonObjectBySingleProperty(final ExecutionContext etk,
        final String elementCode, final Object value) {
        return etk.getDynamicObjectService().<T> getBySingleElementCriteria(elementCode, value).stream()
            .collect(StreamUtils.toSingletonCollector());
    }
}
