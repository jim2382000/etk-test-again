package net.micropact.aea.core.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

public class TempDirectory implements AutoCloseable {

    private final Path path;

    public TempDirectory(final String prefix) {
        try {
            path = Files.createTempDirectory(prefix);
        } catch (final IOException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void close() {
        FileUtils.deleteQuietly(path.toFile());
    }
}
