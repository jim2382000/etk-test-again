package net.micropact.aea.core.deserializer;

import java.util.Optional;

public class NullDefaultingDeserializer<T> implements IDeserializer<T> {

	private final String defaultValue;
	private final IDeserializer<T> wrappedDeserializer;

	public NullDefaultingDeserializer(final String theDefaultValue, final IDeserializer<T> theWrappedDeserializer) {
		defaultValue = theDefaultValue;
		wrappedDeserializer = theWrappedDeserializer;
	}

	@Override
	public T deserialize(final String value) {
		final String defaultedValue = Optional.ofNullable(value)
				.orElse(defaultValue);

		return wrappedDeserializer.deserialize(defaultedValue);
	}
}
