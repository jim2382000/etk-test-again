package net.micropact.aea.core.utility;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with streams.
 *
 * @author Zachary.Miller
 */
public final class StreamUtils {

    /**
     * Utility classes do not need public constructors.
     */
    private StreamUtils() {
    }

    /**
     * Splits/Partitions a stream into separate streams each of a maximum size.
     *
     * @param <T>
     *            the type of the stream
     * @param chunkSize
     *            the chunk size
     * @param stream
     *            the stream
     * @return the chunked stream
     */
    public static <T> Stream<Stream<T>> chunk(final long chunkSize, final Stream<T> stream) {
        final AtomicLong counter = new AtomicLong();
        return stream.collect(Collectors.groupingBy(x -> counter.getAndIncrement() / chunkSize))
            .values()
            .stream()
            .map(List::stream);
    }

    public static <T> Collector<T, ?, T> toSingletonCollector() {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                if (list.size() != 1) {
                    return null;
                }
                return list.get(0);
            });
    }

    public static <T,K,U> Collector<T,?, Map<K,U>> toTreeMapCollector(
    		final Function<? super T,? extends K> keyMapper,
    		final Function<? super T,? extends U> valueMapper) {
		return Collectors.toMap(
				keyMapper,
				valueMapper,
				(v1,v2) -> { throw new GeneralRuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));},
		        TreeMap::new);
	}
}
