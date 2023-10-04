package net.entellitrak.aea.gl.api.java.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class containing utility functionality for dealing with {@link Stream}.
 *
 * @author Zachary.Miller
 */
public final class StreamUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private StreamUtil() {
    }

    /**
     * Take items from a stream as long as a predicate returns true.
     * Once the predicate returns false, the stream will be truncated.
     *
     * @param <T> the type of the items in the stream
     * @param stream the stream
     * @param predicate the predicate
     * @return the truncated stream
     */
    public static <T> Stream<T> takeWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
    }

    /**
     * Take items from a spliterator as long as a predicate returns true.
     * Once the predicate returns false, the spliterator will be truncated.
     *
     * @param <T> the type of the items in the spliterator
     * @param spliterator the spliterator
     * @param predicate the predicate
     * @return the truncated spliterator
     */
    private static <T> Spliterator<T> takeWhile(final Spliterator<T> spliterator, final Predicate<? super T> predicate) {
        return new Spliterators.AbstractSpliterator<>(spliterator.estimateSize(), 0) {

            private boolean stillGoing = true;

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                if (stillGoing) {
                    final boolean hadNext = spliterator.tryAdvance(elem -> {
                        if (predicate.test(elem)) {
                            consumer.accept(elem);
                        } else {
                            stillGoing = false;
                        }
                    });
                    return hadNext && stillGoing;
                }
                return false;
            }
        };
    }
}
