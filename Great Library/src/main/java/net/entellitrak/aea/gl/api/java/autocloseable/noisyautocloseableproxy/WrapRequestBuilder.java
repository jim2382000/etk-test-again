package net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy.INoisyAutoCloseableProxy.IWrapRequest;
import net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy.INoisyAutoCloseableProxy.IWrapRequestBuilder;

class WrapRequestBuilder<T extends AutoCloseable> implements IWrapRequestBuilder<T>{

	private final ExecutionContext etk;
	private final T autoCloseable;
	private final Class<T> interfaceClass;

	WrapRequestBuilder(final ExecutionContext executionContext) {
		etk = executionContext;
		autoCloseable = null;
		interfaceClass = null;
	}

	private WrapRequestBuilder(final ExecutionContext executionContext, final T theAutoCloseable, final Class<T> theInterfaceClass) {
		etk = executionContext;
		autoCloseable = theAutoCloseable;
		interfaceClass = theInterfaceClass;
	}

	@Override
	public IWrapRequestBuilder<T> setAutoCloseable(final T theAutoCloseable) {
		return new WrapRequestBuilder<>(etk, theAutoCloseable, interfaceClass);
	}

	@Override
	public IWrapRequestBuilder<T> setInterfaceClass(final Class<T> theInterfaceClass) {
		return new WrapRequestBuilder<>(etk, autoCloseable, theInterfaceClass);
	}

	@Override
	public IWrapRequest<T> build() {
		return new WrapRequest<>(etk, autoCloseable, interfaceClass);
	}
}