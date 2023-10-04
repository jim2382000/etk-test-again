package net.entellitrak.aea.gl.api.java.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class makes it easier to manage temporary java files by implementing them
 * as {@link AutoCloseable} so that they may be used in try-with-resources.
 * The constructor creates a temporary file and the {@link #close()} method will
 * clean it up.
 *
 * @author Zachary.Miller
 */
public final class TempFile implements AutoCloseable {

    private final Path path;

    /**
     * Create a new temporary file.
     * The file will exist on the file system by the time the constructor returns.
     *
     * @param prefix the file prefix
     * @param suffix the file suffix
     */
    public TempFile(final String prefix, final String suffix) {
        try {
            path = Files.createTempFile(prefix, suffix);
        } catch (final IOException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get a {@link Path} of the temporary file.
     *
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Clean up resources, including deleting the file from the file system.
     */
    @Override
    public void close() {
        FileUtils.deleteQuietly(path.toFile());
    }
}
