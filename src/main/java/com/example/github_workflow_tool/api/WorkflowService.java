package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.*;

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
    public Map<Long, WorkflowRunData> queryApi() throws APIException, CLIException {
        Map<Long, WorkflowRunData> result = new HashMap<>();
        WorkflowResponse workflowResponse = this.workflowClient.fetchData();
        for (WorkflowRun run : workflowResponse.workflowRuns()) {
            JobResponse jobResponse = this.jobClient.fetchData(run.id());
            Map<Long, Job> jobsMap = new HashMap<>();
            for (Job job : jobResponse.jobs()) {
                jobsMap.put(job.id(), job);
            }
            result.put(run.id(), new WorkflowRunData(run, jobsMap));
        }
        return result;
    }

    public Map<Long, WorkflowRunData> askForAdditionalRunData(
            Map<Long, WorkflowRunData> initialState,
            List<WorkflowRun> additionalRuns
    ) {
        Map<Long, WorkflowRunData> result = new HashMap<>(initialState);

        for (WorkflowRun run : additionalRuns) {
            JobResponse jobResponse = this.jobClient.fetchData(run.id());
            Map<Long, Job> jobsMap = new HashMap<>();
            for (Job job : jobResponse.jobs()) {
                jobsMap.put(job.id(), job);
            }
            result.put(run.id(), new WorkflowRunData(run, jobsMap));
        }

        return result;
    }
}
