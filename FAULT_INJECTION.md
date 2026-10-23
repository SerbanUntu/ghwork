# Fault injection

The tests in `GhworkApplicationTests` run the whole application against a fake GitHub API
(through [WireMock](https://wiremock.org/)) that returns errors, broken responses, or no response at all.

Below is a list of features that were added to improve error handling, as well as bugs discovered after adding the tests.

- Every `4xx` status was reported as a user input error, even the ones that mean "try again later" (e.g., 429).
- Any error from the jobs route crashed the app with a stack trace (due to `.exceptionally()`).
- Invalid JSON crashed the app with a stack trace.
- Network errors crashed the app with a stack trace.
- Temporary errors stopped the app instead of being retried.
- Timeout and exponential backoff parameters were not specified.
