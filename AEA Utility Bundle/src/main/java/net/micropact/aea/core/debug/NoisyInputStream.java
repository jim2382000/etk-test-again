package net.micropact.aea.core.debug;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.entellitrak.ExecutionContext;

/**
 * This class is intended to help in debugging whether {@link InputStream}s are being properly closed.
 * It allows you to wrap an {@link InputStream} in a {@link NoisyInputStream}. Doing so will write to the log
 * when the {@link NoisyInputStream} is created and give it a unique id. It will then write to the log when the
 * close method is called.
 *
 * @author zmiller
 */
// Suppress warning about not implementing read method since we don't actually want to modify the stream.
@SuppressWarnings("squid:S4929")
public class NoisyInputStream extends FilterInputStream {

    private final ExecutionContext etk;
    private final UUID name;

    /**
     * Constructs a {@link NoisyInputStream} with a random id and writes it to the logs.
     *
     * @param executionContext entellitrak execution context
     * @param inputStream The {@link InputStream} which is to be wrapped by the {@link NoisyInputStream}
     */
    public NoisyInputStream(final ExecutionContext executionContext, final InputStream inputStream) {
        super(inputStream);
        etk = executionContext;
        name = UUID.randomUUID();
        etk.getLogger().error(String.format("Creating NoisyInputStream with name \"%s\"", name));
    }

    @Override
    public void close() throws IOException{
    	etk.getLogger().error(String.format("Beginning to close NoisyInputStream with name \"%s\"", name));
        super.close();
        etk.getLogger().error(String.format("Finishing closing NoisyInputStream with name \"%s\"", name));
    }
}
