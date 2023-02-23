package org.networkingUtilities.jobs;

import org.networkingUtilities.config.DaggerJobRunnerComponent;

import java.util.List;
import javax.inject.Inject;

public class JobRunner {

    public static final int MAX_RETRIES = 0;
    public static final int BACKOFF_IN_SECONDS = 30;

    @Inject
    ServerHealthJob serverHealthJob;

    @Inject
    DynamicDnsJob dynamicDnsJob;

    public JobRunner() {
        // Set up field injections
        DaggerJobRunnerComponent.builder().build().inject(this);
    }

    public void runJob(final JobType jobType, final List<String> arguments) {
        boolean wasSuccessful;
        switch (jobType) {
            case SERVER_HEALTH:
                wasSuccessful = this.serverHealthJob.runJob(arguments);
                break;
            case DYNAMIC_DNS:
                wasSuccessful = this.dynamicDnsJob.runJob(arguments);
                break;
            default:
                System.out.printf("Unhandled job run type: %s%n", jobType);
                wasSuccessful = false;
                break;
        }
        System.out.printf("Job run was successful: %s%n", wasSuccessful);
    }
}
