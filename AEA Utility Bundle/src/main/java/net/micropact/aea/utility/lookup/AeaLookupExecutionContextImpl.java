/**
 *
 * Lookup Execution Context Impl
 *
 * administrator 09/15/2014
 **/

package net.micropact.aea.utility.lookup;

import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.lookup.For;
import com.entellitrak.lookup.LookupTrackingParameters;
import com.micropact.entellitrak.lookup.LookupTrackingParametersBuilder;
import com.micropact.entellitrak.system.ServiceUserContainer;
import com.micropact.entellitrak.system.UserContainer;
import com.micropact.entellitrak.web.RequestContextHolder;

public class AeaLookupExecutionContextImpl extends com.micropact.entellitrak.lookup.LookupExecutionContextImpl {

	// Suppress warning about number of parameters. We would rather be immutable.
    @SuppressWarnings("java:S107")
    public AeaLookupExecutionContextImpl (final ExecutionContext theContext,
            final boolean isForTracking,
            final boolean isForSearch,
            final boolean isForView,
            final Long trackingId,
            final Long baseId,
            final Long parentId,
            final String dataObjectBusinessKey,
            final String dataObjectTableName) {

        super(theContext, getUserContainer(),
                isForTracking ? For.TRACKING.getType() :
                    isForSearch ? For.SEARCH.getType() :
                        isForView? For.VIEW.getType() : For.NONE.getType(),
                                getLookupTrackingParameters(trackingId, baseId, parentId, dataObjectBusinessKey, dataObjectTableName)
                );
    }

    public AeaLookupExecutionContextImpl (final ExecutionContext theContext,
            final For isForVar,
            final Long trackingId,
            final Long baseId,
            final Long parentId,
            final String dataObjectBusinessKey,
            final String dataObjectTableName) {
        super(theContext, getUserContainer(), isForVar.getType(),
                getLookupTrackingParameters(trackingId, baseId, parentId, dataObjectBusinessKey, dataObjectTableName));
    }

    @Override
    public boolean isFor(final String isForVar) {
        return "liveSearch".equalsIgnoreCase(isForVar);
    }

    private static LookupTrackingParameters getLookupTrackingParameters(final Long trackingId, final Long baseId, final Long parentId, final String businessKey, final String tableName){
        final LookupTrackingParametersBuilder builder = new LookupTrackingParametersBuilder();
        builder.setBaseId(baseId);
        builder.setDataObjectBusinessKey(businessKey);
        builder.setDataObjectTableName(tableName);
        builder.setParentId(parentId);
        builder.setTrackingId(trackingId);
        return builder.build();
    }

    private static UserContainer getUserContainer() {
        /* We prefer the container coming from a web request, but it is not available in situations like endpoints, so we'll use a default one
         * in those cases. */
        return Optional.ofNullable(RequestContextHolder.getUserContainer())
                .orElseGet(ServiceUserContainer::new);
    }
}
