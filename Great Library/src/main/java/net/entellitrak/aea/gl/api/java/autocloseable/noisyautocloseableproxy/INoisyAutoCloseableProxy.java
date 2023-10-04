package net.entellitrak.aea.gl.api.java.autocloseable.noisyautocloseableproxy;

import java.lang.StackWalker.StackFrame;
import java.lang.ref.Cleaner;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.ref.CleanerUtil;

/**
 * This interface allows you to wrap an instance of an {@link AutoCloseable}
 * object that will log if the garbage collector attempts to collect it without
 * the close() method being called. It can be useful for debugging resource leaks.
 *
 * The interface accomplishes this using {@link Proxy}, and is subject to any limitations
 * of that class, the largest of them is that the generic for the proxy
 * <strong>must be an interface instead of a class.</strong>
 *
 * @author Zachary.Miller
 */
public interface INoisyAutoCloseableProxy {

	/**
	 * Creates a proxy for an {@link AutoCloseable} that logs if the object
	 * becomes eligible for garbage collection before being closed.
	 *
	 * @param <T> The type to return. <strong>This must be an interface, not a class.</strong>
	 * @param wrapRequest the request
	 * @return the proxy
	 */
	@SuppressWarnings("resource")
	public static <T extends AutoCloseable> T wrap(final IWrapRequest<T> wrapRequest) {
		final String stacktrace = StackWalker.getInstance()
				.walk(stackFrames
						-> stackFrames
						.map(StackFrame::toString)
						.collect(Collectors.joining("\n")));

		final T autoCloseable = wrapRequest.getAutoCloseable();
		final Class<T> interfaceClass = wrapRequest.getInterfaceClass();

		final DynamicInvocationHandler invocationHandler = new DynamicInvocationHandler(wrapRequest.getExecutionContext(), autoCloseable, stacktrace);

		final Class<? extends AutoCloseable> theActualClass = autoCloseable.getClass();

		if(!interfaceClass.isInterface()) {
			throw new GeneralRuntimeException(String.format("We can only create proxies for interfaces, not classes. %s is not an interface.",
					interfaceClass.getName()));
		} else if(!interfaceClass.isAssignableFrom(theActualClass)) {
			throw new GeneralRuntimeException(
					String.format("We have create an instance of %s from a %s, but it does not implement the interface.",
							interfaceClass,
							theActualClass.getName()));
		} else{
			final T proxy = (T) Proxy.newProxyInstance(
					theActualClass.getClassLoader(),
					new Class[] {interfaceClass},
					invocationHandler);

			invocationHandler.registerProxy(proxy);

			return proxy;
		}
	}

	static class DynamicInvocationHandler implements InvocationHandler {
		private final ExecutionContext etk;
		private final AutoCloseable autoCloseable;
		private final CleaningState cleaningState;
		private Cleaner.Cleanable cleanable;

		public DynamicInvocationHandler(final ExecutionContext executionContext, final AutoCloseable theAutoCloseable, final String stacktrace) {
			etk = executionContext;
			autoCloseable = theAutoCloseable;
			/* Suppress warning about using newlines in string */
			@SuppressWarnings("java:S3457")
			final String message = String.format("WARNING!!! An AutoCloseable was not closed.\nWrapped Class: %s,\nAllocation Stacktrace: %s", theAutoCloseable.getClass().getName(), stacktrace);
			cleaningState = new CleaningState(etk, message);
		}

		public void registerProxy(final AutoCloseable proxy) {
			cleanable = CleanerUtil.getSharedCleaner().register(proxy, cleaningState);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if(method.getName().equals("close") && method.getParameterCount() == 0) {
				cleaningState.setClosed();
				cleanable.clean();
			}

			return method.invoke(autoCloseable, args);
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

	public interface IWrapRequest<T extends AutoCloseable> {

		ExecutionContext getExecutionContext();

		Class<T> getInterfaceClass();

		T getAutoCloseable();

		public static <U extends AutoCloseable> IWrapRequestBuilder<U> builder(final ExecutionContext etk) {
			return new WrapRequestBuilder<>(etk);
		}
	}

	public interface IWrapRequestBuilder<T extends AutoCloseable> {
		IWrapRequestBuilder<T> setAutoCloseable(T autoCloseable);

		IWrapRequestBuilder<T> setInterfaceClass(Class<T> class1);

		IWrapRequest<T> build();
	}
}
