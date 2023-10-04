package net.entellitrak.aea.gl.api.java.io.noisyinputstream;

import java.io.InputStream;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream.IWrapRequest;

class WrapRequest implements IWrapRequest{

	private final ExecutionContext etk;
	private final InputStream inputStream;

	WrapRequest(final ExecutionContext executionContext, final InputStream theInputStream) {
		etk = executionContext;
		inputStream = theInputStream;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return etk;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}
}
