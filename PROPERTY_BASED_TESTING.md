# Property-based testing

Below is a list of bugs found in `DiffingService` by the property tests in `DiffingServiceProperties`:

- Null run, job and step statuses resulted in a `NullPointerException`.
- Steps that were not completed (e.g., `pending` status) had their conclusion checked.
- Steps were paired by list position instead of step number. When a step was missing from a response, every following step was compared with the wrong one.
- Runs that were not queued yet (e.g., `pending`) were marked as `AFTER_QUEUEING`.
