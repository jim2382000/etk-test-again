package net.entellitrak.aea.exception;

public class PdfGenerateRuntimeException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 6798881775203538343L;

	/**
	 * Constructor.
	 *
	 * @param e
	 *            The underlying {@link Exception}
	 */
	public PdfGenerateRuntimeException(final Exception e) {
		super(e);
	}

	/**
	 * Constructor.
	 *
	 * @param msg
	 *            Description of what went wrong
	 */
	public PdfGenerateRuntimeException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 *
	 * @param msg
	 *            Description of what went wrong
	 * @param e
	 *            The underlying {@link Exception}
	 */
	public PdfGenerateRuntimeException(final String msg, final Exception e) {
		super(msg, e);
	}
}
