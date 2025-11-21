package com.personal.networkingUtilities.jobs;

import com.personal.networkingUtilities.component.DaggerJobRunnerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.inject.Inject;

public class JobRunner {

    public static final int MAX_RETRIES = 0;
    public static final int BACKOFF_IN_SECONDS = 30;

    @Inject
    ServerHealthJob serverHealthJob;

    @Inject
    DynamicDnsJob dynamicDnsJob;

    private static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

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
                logger.error("Unhandled job run type: {}", jobType);
                wasSuccessful = false;
                break;
        }
        logger.info("Job run was successful: {}", wasSuccessful);
    }
}
