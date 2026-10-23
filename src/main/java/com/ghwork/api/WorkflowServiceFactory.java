package com.ghwork.api;

import com.ghwork.domain.AccessToken;
import com.ghwork.domain.Repository;
import org.springframework.stereotype.Component;

/**
 * Factory pattern for {@link WorkflowService} objects.
 */
@Component
public class WorkflowServiceFactory {

    private final JobClientFactory jobClientFactory;
    private final WorkflowClientFactory workflowClientFactory;

    /**
     * Constructor for a factory that builds {@link WorkflowService} objects.
     *
     * @param jobClientFactory      The factory for the client that fetches job data.
     * @param workflowClientFactory The factory for the client that fetches workflow data.
     */
    public WorkflowServiceFactory(
        JobClientFactory jobClientFactory,
        WorkflowClientFactory workflowClientFactory
    ) {
        this.jobClientFactory = jobClientFactory;
        this.workflowClientFactory = workflowClientFactory;
    }

    /**
     * Build a {@link WorkflowService} with the given repository and access token.
     *
     * @param repository  The repository to fetch data from.
     * @param accessToken The user's personal access token.
     * @return A new {@link WorkflowService} object.
     */
    public WorkflowService create(Repository repository, AccessToken accessToken) {
        return new WorkflowService(
            jobClientFactory.create(repository, accessToken),
            workflowClientFactory.create(repository, accessToken)
        );
    }
}
