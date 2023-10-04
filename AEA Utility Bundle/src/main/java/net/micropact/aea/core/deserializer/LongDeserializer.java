package net.micropact.aea.core.deserializer;

import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.utility.Utility;

/**
 * This class is capable of turning a value into a Long.
 *
 * @author zmiller
 */
public class LongDeserializer implements IDeserializer<Long> {

    private final Long defaultValue;

    /**
     * Constructs a {@link LongDeserializer} which will return theDefaultValue if the String it attempts to deserialize
     * is blank.
     *
     * @param theDefaultValue the default value which should be used if the value was blank.
     */
    public LongDeserializer(final Long theDefaultValue){
        defaultValue = theDefaultValue;
    }

    @Override
    public Long deserialize(final String value) {
        return Utility.isBlank(value)
                ? defaultValue
                : Coersion.toLong(value);
    }
}
