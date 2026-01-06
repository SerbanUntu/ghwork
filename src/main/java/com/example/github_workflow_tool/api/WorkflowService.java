package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Job;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.domain.WorkflowRun;

import java.util.*;

/**
 * Abstraction around multiple API calls. Exposes a method to map workflow data to respective job data.
 */
public class WorkflowService {

    private final JobClient jobClient;
    private final WorkflowClient workflowClient;

    public WorkflowService(Repository repository, AccessToken accessToken) throws APIException {
        this.jobClient = new JobClient(repository, accessToken);
        this.workflowClient = new WorkflowClient(repository, accessToken);
    }

    /**
     * Returns a mapping of workflow runs to their respective job runs for the provided GitHub repository.
     * Makes multiple API requests under the hood.
     *
     * @return A mapping of workflow runs to their respective job runs.
     * @throws APIException If a network fault occurs.
     * @throws CLIException If the repository name or access token provided by the user are invalid.
     */
    public Map<WorkflowRun, List<Job>> queryApi() throws APIException, CLIException {
        Map<WorkflowRun, List<Job>> result = new HashMap<>();
        WorkflowResponse workflowResponse = this.workflowClient.fetchData();
        for (WorkflowRun run : workflowResponse.workflowRuns()) {
            JobResponse jobResponse = this.jobClient.fetchData(run.id());
            result.put(run, jobResponse.jobs());
        }
        return result;
    }
}
