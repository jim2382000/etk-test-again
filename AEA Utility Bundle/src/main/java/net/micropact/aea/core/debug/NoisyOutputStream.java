package net.micropact.aea.core.debug;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.entellitrak.ExecutionContext;

/**
 * This class is intended to help in debugging whether {@link OutputStream}s are being properly closed.
 * It allows you to wrap an {@link OutputStream} in a {@link NoisyOutputStream}. Doing so will write to the log
 * when the {@link NoisyOutputStream} is created and give it a unique id. It will then write to the log when the
 * close method is called.
 *
 * @author zmiller
 */
//Suppress warning about not implementing write method since we don't actually want to modify the stream.
@SuppressWarnings("squid:S4349")
public class NoisyOutputStream extends FilterOutputStream {

    private final ExecutionContext etk;
    private final UUID name;

    /**
     * Constructs a {@link NoisyOutputStream} with a random id and logs that it was created.
     *
     * @param executionContext entellitrak execution context
     * @param outputStream The {@link OutputStream} which is to be wrapped by the {@link NoisyOutputStream}
     */
    public NoisyOutputStream(final ExecutionContext executionContext, final OutputStream outputStream) {
        super(outputStream);
        etk = executionContext;
        name = UUID.randomUUID();
        etk.getLogger().error(String.format("Creating NoisyOutputStream with name \"%s\"", name));
    }

    @Override
    public void close() throws IOException{
    	etk.getLogger().error(String.format("Beginning to close NoisyOutputStream with name \"%s\"", name));
        super.close();
        etk.getLogger().error(String.format("Finishing closing NoisyOutputStream with name \"%s\"", name));
    }
}
