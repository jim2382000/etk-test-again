package net.entellitrak.aea.gl.api.java.map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for more easily building {@link Map}s.
 * It is a thin wrapper around a mutable underlying map.
 *
 * @author Zachary.Miller
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class MapBuilder<K, V> {

    private final Map<K, V> map;

    /**
     * Simple constructor.
     */
    public MapBuilder() {
        map = new LinkedHashMap<>();
    }

    /**
     * Put a value into the map.
     *
     * @param key the key
     * @param value the value
     * @return this
     */
    public MapBuilder<K, V> put(final K key, final V value) {
        map.put(key, value);

        return this;
    }

    /**
     * Get the map.
     *
     * @return the map
     */
    public Map<K, V> build() {
        return map;
    }
}
