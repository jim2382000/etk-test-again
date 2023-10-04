package net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy.INoisyAutoCloseableProxy.IWrapRequest;

class WrapRequest<T extends AutoCloseable> implements IWrapRequest<T> {

	private final ExecutionContext etk;
	private final T autoCloseable;
	private final Class<T> interfaceClass;

	WrapRequest(final ExecutionContext executionContext, final T theAutoCloseable, final Class<T> theInterfaceClass) {
		etk = executionContext;
		autoCloseable = theAutoCloseable;
		interfaceClass = theInterfaceClass;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return etk;
	}

	@Override
	public T getAutoCloseable() {
		return autoCloseable;
	}

	@Override
	public Class<T> getInterfaceClass() {
		return interfaceClass;
	}
}