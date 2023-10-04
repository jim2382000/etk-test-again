package net.micropact.aea.core.common.export;

/**
 * Data Transfer Object for AEA Core Configuration.
 *
 * @author Zachary.Miller
 */
public class AeaCoreConfigurationDTO {

    private final String code;
    private final String description;
    private final String value;

    /**
     * Simple constructor.
     *
     * @param theCode the code
     * @param theDescription the description
     * @param theValue the value
     */
    public AeaCoreConfigurationDTO(
            final String theCode,
            final String theDescription,
            final String theValue) {
        code = theCode;
        description = theDescription;
        value = theValue;
    }

    /**
     * Get the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
