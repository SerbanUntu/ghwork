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

    public Map<Long, WorkflowRunData> mapJobsToWorkflows(
            List<WorkflowRun> runs,
            List<JobResponse> jobResponses
    ) {
        Map<Long, WorkflowRunData> result = new HashMap<>();

        for (var run : runs) {
            result.put(run.id(), new WorkflowRunData(run, new HashMap<>()));
        }

        for (var response : jobResponses) {
            for (var job : response.jobs()) {
                result.get(job.runId()).jobs().put(job.id(), job);
            }
        }

        return result;
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
        WorkflowResponse workflowResponse = this.workflowClient.fetchData();
        List<Long> runIds = workflowResponse.workflowRuns().stream().map(WorkflowRun::id).toList();
        List<JobResponse> jobResponses = this.jobClient.fetchData(runIds);
        return mapJobsToWorkflows(workflowResponse.workflowRuns(), jobResponses);
    }

    public Map<Long, WorkflowRunData> askForAdditionalRunData(
            Map<Long, WorkflowRunData> initialState,
            List<WorkflowRun> additionalRuns
    ) {
        Map<Long, WorkflowRunData> result = new HashMap<>(initialState);
        List<Long> runIds = additionalRuns.stream().map(WorkflowRun::id).toList();
        List<JobResponse> jobResponses = this.jobClient.fetchData(runIds);
        result.putAll(mapJobsToWorkflows(additionalRuns, jobResponses));
        return result;
    }
}
