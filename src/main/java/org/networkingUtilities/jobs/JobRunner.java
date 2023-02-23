package org.networkingUtilities.jobs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import org.networkingUtilities.config.DaggerJobRunnerComponent;
import org.networkingUtilities.config.JobRunnerComponent;
import org.networkingUtilities.utils.DiscordWebhook;

import javax.inject.Inject;
import java.util.List;

@Builder
@SuppressFBWarnings("EI_EXPOSE_REP") // Lombok generated arguments method
public class JobRunner {

    public static final int MAX_RETRIES = 3;
    public static final int BACKOFF_IN_SECONDS = 30;

    private final JobType jobType;
    private final List<String> arguments;
    @Builder.Default
    private final DiscordWebhook discordWebhook = DiscordWebhook.builder().build();

    @Inject
    ServerHealthJob serverHealthJob;

    @Inject
    DynamicDnsJob dynamicDnsJob;

    public void runJob() {
        // Set up field injections
        DaggerJobRunnerComponent.builder().build().inject(this);

        boolean wasSuccessful;
        switch (this.jobType) {
            case SERVER_HEALTH:
                wasSuccessful = this.serverHealthJob.runJob(this.arguments);
                break;
            case DYNAMIC_DNS:
                wasSuccessful = this.dynamicDnsJob.runJob(this.arguments);
                break;
            default:
                System.out.printf("Unhandled job run type: %s%n", this.jobType);
                wasSuccessful = false;
                break;
        }
        System.out.printf("Job run was successful: %s%n", wasSuccessful);
    }
}
