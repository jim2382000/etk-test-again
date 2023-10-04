package net.entellitrak.aea.gl.api.java.io.noisyinputstream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.Cleaner;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.ref.CleanerUtil;

public interface INoisyInputStream {

	public static InputStream wrap(final IWrapRequest wrapRequest) {
		return new NoisyInputStream(wrapRequest);
	}

	// Suppress warning about not providing a read method. That's not the reason we're subclassing.
	@SuppressWarnings("java:S4929")
	static class NoisyInputStream extends FilterInputStream {

		private final ExecutionContext etk;
		private final CleaningState cleaningState;
		private final Cleaner.Cleanable cleanable;

		NoisyInputStream(final IWrapRequest wrapRequest) {
			super(wrapRequest.getInputStream());

			etk = wrapRequest.getExecutionContext();

			final String stacktrace = StackWalker.getInstance()
					.walk(stackFrames
							-> stackFrames
							.map(StackFrame::toString)
							.collect(Collectors.joining("\n")));

			/* Suppress warning about using newlines in string */
			@SuppressWarnings("java:S3457")
			final String message = String.format("WARNING!!! An InputStream was not closed,\nAllocation Stacktrace: %s", stacktrace);
			cleaningState = new CleaningState(etk, message);
			cleanable = CleanerUtil.getSharedCleaner().register(this, cleaningState);
		}

		@Override
		public void close() throws IOException {
			cleaningState.setClosed();
			cleanable.clean();

			super.close();
		}
	}

	static class CleaningState implements Runnable {

		private final ExecutionContext etk;
		private final String message;
		private boolean closed = false;

		CleaningState(final ExecutionContext executionContext, final String theMessage) {
			etk = executionContext;
			message = theMessage;
		}

		void setClosed() {
			closed = true;
		}

		@Override
		public void run() {
			if(!closed) {
				etk.getLogger().error(message);
			}
		}
	}

	public static interface IWrapRequest {

		static IWrapRequestBuilder builder(final ExecutionContext etk) {
			return new WrapRequestBuilder(etk);
		}

		ExecutionContext getExecutionContext();

		InputStream getInputStream();
	}

	public static interface IWrapRequestBuilder {
		IWrapRequestBuilder setInputStream(InputStream inputStream);

		IWrapRequest build();
	}
}
