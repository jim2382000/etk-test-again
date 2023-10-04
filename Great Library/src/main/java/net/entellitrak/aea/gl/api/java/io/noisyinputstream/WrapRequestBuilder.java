package net.entellitrak.aea.gl.api.java.io.noisyinputstream;

import java.io.InputStream;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream.IWrapRequest;
import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream.IWrapRequestBuilder;

class WrapRequestBuilder implements IWrapRequestBuilder{

	private final ExecutionContext etk;
	private final InputStream inputStream;

	public WrapRequestBuilder(final ExecutionContext executionContext) {
		this(executionContext, null);
	}

	public WrapRequestBuilder(final ExecutionContext executionContext, final InputStream theInputStream) {
		etk = executionContext;
		inputStream = theInputStream;
	}

	@Override
	public IWrapRequestBuilder setInputStream(final InputStream theInputStream) {
		return new WrapRequestBuilder(etk, theInputStream);
	}

	@Override
	public IWrapRequest build() {
		return new WrapRequest(etk, inputStream);
	}
}
