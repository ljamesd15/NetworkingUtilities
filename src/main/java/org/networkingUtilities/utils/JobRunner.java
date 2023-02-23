package org.networkingUtilities.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import org.networkingUtilities.localServer.serverChecker.ServerLivenessChecker;

@Builder
@SuppressFBWarnings("EI_EXPOSE_REP") // Lombok generated arguments method
public class JobRunner {

    public enum JobRunType {
        LIVENESS_CHECK("LivenessCheck"),
        DYNAMIC_DNS("DynamicDns");

        public final String label;

        JobRunType(final String label) {
            this.label = label;
        }

        public static JobRunType fromString(String label) {
            for (JobRunType jobRunType : JobRunType.values()) {
                if (jobRunType.label.equalsIgnoreCase(label)) {
                    return jobRunType;
                }
            }
            throw new IllegalArgumentException(String.format("No JobRunType with label %s found.", label));
        }
    }

    private static final int MAX_RETRIES = 3;
    private static final int BACKOFF_IN_SECONDS = 30;

    private final JobRunType jobRunType;
    private final List<String> arguments;
    @Builder.Default
    private final DiscordWebhook discordWebhook = DiscordWebhook.builder().build();

    public void runJob() {
        switch (this.jobRunType) {
            case LIVENESS_CHECK:
                final String hostname = this.arguments.get(0);
                int port;
                try {
                    port = Integer.parseInt(this.arguments.get(1));
                } catch (NumberFormatException ex) {
                    System.out.printf("Unable to parse integer from %s%n", this.arguments.get(1));
                    return;
                }
                final Optional<String> maybeServerRestartFile =
                        Optional.ofNullable(this.arguments.size() > 2 ? this.arguments.get(2) : null);
                final ServerLivenessChecker serverLivenessChecker = ServerLivenessChecker.builder()
                        .hostname(hostname)
                        .port(port)
                        .serverRestartFilePath(maybeServerRestartFile)
                        .build();
                this.checkServerLiveness(serverLivenessChecker, MAX_RETRIES);
                return;

            default:
                System.out.printf("Unhandled job run type: %s%n", this.jobRunType);
        }
    }



    private void checkServerLiveness(final ServerLivenessChecker serverLivenessChecker, final int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException(String.format("Retries %d, cannot be less than 0%n", retries));
        }

        if (!serverLivenessChecker.isServerAvailable()) {
            if (retries == 0) {
                final String failureMessage =
                        String.format("{\"content\": \"Server %s is unavailable, attempting server restart\"}", serverLivenessChecker);
                this.discordWebhook.sendDiscordMessage(failureMessage);
                final boolean restartedSuccessfully = serverLivenessChecker.restartServer();
                if (!restartedSuccessfully) {
                    final String failedToRestartMessage =
                            String.format("{\"content\": \"Failed to restart server %s\"}", serverLivenessChecker);
                    this.discordWebhook.sendDiscordMessage(failedToRestartMessage);
                }
            } else {
                System.out.printf("Server liveness check failed for %s, sleeping for %d seconds%n",
                        serverLivenessChecker, BACKOFF_IN_SECONDS);
                try {
                    Thread.sleep(BACKOFF_IN_SECONDS * 1000);
                } catch (InterruptedException ex) {
                    System.out.println("Error while sleeping after failed server connection. Failing job run");
                    ex.printStackTrace();
                    return;
                }
                checkServerLiveness(serverLivenessChecker, retries - 1);
            }
        } else {
            System.out.printf("Server: %s was available%n", serverLivenessChecker);
        }
    }
}
