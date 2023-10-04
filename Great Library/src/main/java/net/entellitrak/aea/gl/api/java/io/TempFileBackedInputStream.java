package net.entellitrak.aea.gl.api.java.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import com.entellitrak.page.FileResponse;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class can create an {@link InputStream} based on a {@link TempFile} in such a way
 * that when the input stream is closed, the temp file will also be deleted.
 * The reason for this is that there are situations where methods have to return an input stream, but
 * do not want to keep the entire contents in memory at once.
 * An example use-case would be returning a large file with {@link FileResponse}.
 *
 * @author Zachary.Miller
 * @see TempFile
 */
public final class TempFileBackedInputStream {

	/**
	 * Utility classes do not need public constructors.
	 */
	private TempFileBackedInputStream() {}

	/**
	 * Create an input stream backed by a temporary file. Ownership of the tempFile is passed
	 * to this method. The caller is responsible for closing the returned input stream.
	 *
	 * @param tempFile the temporary file
	 * @return the input stream
	 */
	public static InputStream createTempFileBackedInputStream(final TempFile tempFile) {
		final InputStream fileInputStream;

		try {
			fileInputStream = new FileInputStream(tempFile.getPath().toFile());
		} catch (final Exception e) {
			tempFile.close();
			throw new GeneralRuntimeException(e);
		}

		return new AutoCloseablesBackedInputStream(fileInputStream, List.of(tempFile));
	}
}
