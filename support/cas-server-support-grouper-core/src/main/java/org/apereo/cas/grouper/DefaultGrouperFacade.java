package org.apereo.cas.grouper;

import org.apereo.cas.util.CollectionUtils;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link DefaultGrouperFacade} that acts as a wrapper
 * in front of the grouper API.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DefaultGrouperFacade implements GrouperFacade {

    @Override
    public Collection<WsGetGroupsResult> getGroupsForSubjectId(final String subjectId) {
        try {
            val results = fetchGroupsFor(subjectId);
            if (results == null || results.length == 0) {
                LOGGER.warn("Subject id [{}] could not be located.", subjectId);
                return new ArrayList<>(0);
            }
            LOGGER.debug("Found [{}] groups for [{}]", results.length, subjectId);
            return CollectionUtils.wrapList(results);
        } catch (final Exception e) {
            LOGGER.warn("Grouper WS did not respond successfully. Ensure your credentials are correct "
                + ", the url endpoint for Grouper WS is correctly configured and the subject [{}] exists in Grouper.", subjectId, e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Fetch groups.
     *
     * @param subjectId the subject id
     * @return the groups
     */
    protected WsGetGroupsResult[] fetchGroupsFor(final String subjectId) {
        val groupsClient = new GcGetGroups().addSubjectId(subjectId);
        return groupsClient.execute().getResults();
    }
}
