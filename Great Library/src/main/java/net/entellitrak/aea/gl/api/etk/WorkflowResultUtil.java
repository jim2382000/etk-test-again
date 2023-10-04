package net.entellitrak.aea.gl.api.etk;

import com.entellitrak.BaseObjectEventContext;
import com.entellitrak.legacy.workflow.WorkflowResult;

/**
 * Utility class for dealing with {@link WorkflowResult}.
 *
 * @author Zachary.Miller
 */
public final class WorkflowResultUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private WorkflowResultUtil() {
    }

    /**
     * Since result.cancelTransaction should always be accompanied by
     * result.addMessage, this function combines the
     * two.
     *
     * @param etk entellitrak execution context.
     * @param message message to be displayed. HTML is NOT escaped.
     */
    public static void cancelTransactionMessage(final BaseObjectEventContext etk, final String message) {
        final WorkflowResult result = etk.getResult();
        result.cancelTransaction();
        result.addMessage(message);
    }
}
