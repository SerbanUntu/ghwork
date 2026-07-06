package com.example.ghwork.api;

import com.example.ghwork.api.exceptions.ApiException;
import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import com.example.ghwork.domain.WorkflowRun;
import com.example.ghwork.domain.WorkflowRunData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstraction around multiple API calls.
 * Exposes a method to map workflow data to respective job data.
 */
public class WorkflowService {

    private final JobClient jobClient;
    private final WorkflowClient workflowClient;

    /**
     * Constructor for a service managing GitHub clients for jobs and workflows.
     *
     * @param repository  The repository to fetch data from.
     * @param accessToken The access token needed to have access to the repository.
     * @throws ApiException If URI construction for the underlying workflow client fails.
     */
    public WorkflowService(Repository repository, AccessToken accessToken) throws ApiException {
        this.jobClient = new JobClient(repository, accessToken);
        this.workflowClient = new WorkflowClient(repository, accessToken);
    }

    /**
     * Maps a list of runs and a list of jobs to an indexed map of {@link WorkflowRunData} objects
     * that contain the jobs for each workflow run.
     *
     * @param runs         The workflow runs from the API
     * @param jobResponses The job responses containing jobs corresponding to the given runs
     * @return A mapping of runs and jobs nested inside
     *     an indexed map of {@link WorkflowRunData} objects
     */
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
     * Returns a mapping of workflow runs to their respective job runs for the
     * provided GitHub repository. Makes multiple API requests under the hood.
     *
     * @return A mapping of workflow runs to their respective job runs.
     * @throws ApiException If a network fault occurs.
     * @throws CliException If the repository name or access token provided by the user are invalid.
     */
    public Map<Long, WorkflowRunData> getCurrentRuns(Set<Long> ignoredRuns)
        throws ApiException, CliException {
        WorkflowResponse workflowResponse = this.workflowClient.fetchData();
        List<Long> runIds = workflowResponse.workflowRuns().stream().map(WorkflowRun::id).toList();
        List<JobResponse> jobResponses = this.jobClient.fetchData(runIds
            .stream()
            .filter(id -> !ignoredRuns.contains(id))
            .toList()
        );
        return mapJobsToWorkflows(workflowResponse.workflowRuns(), jobResponses);
    }

    /**
     * Augments the provided initial list of runs to include runs that were in the stored state
     * but not returned by the API call, presumably because they are completed.
     *
     * @param initialState   The list of runs saved by the application
     * @param additionalRuns The list of runs to fetch the details for, from the API
     * @return A mapping of workflow runs to their respective job runs, including runs
     *     and jobs that were in the initial state but not in the new API state.
     * @throws ApiException If a network fault occurs.
     * @throws CliException If the repository name or access token provided by the user are invalid.
     */
    public Map<Long, WorkflowRunData> askForAdditionalRunData(
        Map<Long, WorkflowRunData> initialState,
        List<WorkflowRun> additionalRuns
    ) throws ApiException, CliException {
        Map<Long, WorkflowRunData> result = new HashMap<>(initialState);
        List<Long> runIds = additionalRuns.stream().map(WorkflowRun::id).toList();
        List<JobResponse> jobResponses = this.jobClient.fetchData(runIds);
        result.putAll(mapJobsToWorkflows(additionalRuns, jobResponses));
        return result;
    }

    /**
     * Returns a set of ids corresponding to runs that have finished, as marked by the
     * {@code conclusion()} field not being null.
     *
     * @param runsState The runs containing jobs, indexed by id
     * @return The runs that have not finished, indexed by id
     */
    public Set<Long> getRunsIdsToIgnore(Map<Long, WorkflowRunData> runsState) {
        return runsState.values()
            .stream()
            .map(WorkflowRunData::run)
            .filter(run -> run.conclusion() != null)
            .map(WorkflowRun::id)
            .collect(Collectors.toSet());
    }

    /**
     * Perform set difference (A - B) on two sets of runs.
     *
     * @param first         The first set of runs, indexed by id and containing jobs.
     * @param second        The second set of runs, indexed by id and containing jobs.
     * @param ignoredRunIds The runs to not be added in the resulting list,
     *                      even if they are present in the first set.
     * @return The set difference of the two sets of runs.
     */
    public List<WorkflowRun> getRunsSetDifference(
        Map<Long, WorkflowRunData> first,
        Map<Long, WorkflowRunData> second,
        Set<Long> ignoredRunIds
    ) {
        return first
            .values()
            .stream()
            .map(WorkflowRunData::run)
            .filter(run -> !second.containsKey(run.id()) && !ignoredRunIds.contains(run.id()))
            .toList();
    }
}
