package net.entellitrak.aea.gl.api.java.io;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.entellitrak.page.FileResponse;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class implements {@link InputStream} as a wrapper around a specific {@link InputStream}
 * that requires that other resources be closed when the inputstream is closed.
 * For instance, it can be used to more easily implement {@link TempFileBackedInputStream}, or
 * an input stream for {@link FileResponse} which would a backing content store, like a temporary file
 * to be cleaned up when core closes the input stream.
 *
 * @author Zachary.Miller
 * @see TempFileBackedInputStream
 */
/* Suppress warning about not overriding read method. We aren't trying to override read. */
@SuppressWarnings("java:S4929")
public final class AutoCloseablesBackedInputStream extends FilterInputStream {

	private final List<AutoCloseable> autoCloseables;

	/**
	 * @param inputStream the input stream
	 * @param theAutoCloseables the other resources which must be cleaned up along with the input stream
	 */
	public AutoCloseablesBackedInputStream(final InputStream inputStream, final List<AutoCloseable> theAutoCloseables) {
		super(inputStream);
		autoCloseables = theAutoCloseables;
	}

	@Override
	public void close() {
		final List<Exception> underlyingExceptions = Arrays.asList();

		try{
			super.close();
		} catch(final Exception e){
			underlyingExceptions.add(e);
		}

		autoCloseables.stream()
		.forEach(autoCloseable -> {
			try {
				autoCloseable.close();
			} catch (final Exception e) {
				underlyingExceptions.add(e);
			}
		});

		if(!underlyingExceptions.isEmpty()) {
			throw new GeneralRuntimeException(String.format("Encountered %s exceptions when closing input streams. Including stacktrace for first exception encountered.", underlyingExceptions.size()),
					underlyingExceptions.get(0));
		}
	}
}
