package org.networkingUtilities.utils;

import org.networkingUtilities.localServer.serverChecker.ServerLivenessChecker;

import java.util.Optional;

public class JobRunner {

    public enum JobRunType {
        LIVENESS_CHECK("LivenessCheck");

        public final String label;

        private JobRunType(final String label) {
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
    private static final String DEFAULT_WEBHOOK_URL =
            "https://discordapp.com/api/webhooks/1077964961105596426/_heFLKBXbX1WW0g53YjlrmRElWV6Go9-4OwQJlHyWPlCIxjGE-xUXZEHo0dPILtJJ0UJ";

    private final JobRunType jobRunType;
    private final String[] arguments;
    private final DiscordWebhook discordWebhook;

    public JobRunner(final JobRunType jobRunType, final String[] jobArguments) {
        this.jobRunType = jobRunType;
        this.arguments = jobArguments;
        this.discordWebhook = new DiscordWebhook(DEFAULT_WEBHOOK_URL);
    }

    public void runJob() {
        switch (this.jobRunType) {
            case LIVENESS_CHECK:
                final String hostname = this.arguments[0];
                int port;
                try {
                    port = Integer.parseInt(this.arguments[1]);
                } catch (NumberFormatException ex) {
                    System.out.printf("Unable to parse integer from %s\n", this.arguments[1]);
                    return;
                }
                final Optional<String> maybeServerRestartFile = Optional.ofNullable(this.arguments.length > 2 ? this.arguments[2] : null);
                final ServerLivenessChecker serverLivenessChecker = new ServerLivenessChecker(
                        hostname,
                        port,
                        maybeServerRestartFile
                );
                this.checkServerLiveness(serverLivenessChecker, MAX_RETRIES);
                return;
            default:
                System.out.printf("Unhandled job run type: %s\n", this.jobRunType);
        }
    }



    private void checkServerLiveness(final ServerLivenessChecker serverLivenessChecker, final int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException(String.format("Retries %d, cannot be less than 0\n", retries));
        }

        if (!serverLivenessChecker.isServerAvailable()) {
            if (retries == 0) {
                final String failureMessage = String.format("{\"content\": \"Server %s is unavailable, attempting server restart\"}", serverLivenessChecker);
                this.discordWebhook.sendDiscordMessage(failureMessage);
                final boolean restartedSuccessfully = serverLivenessChecker.restartServer();
                if (!restartedSuccessfully) {
                    final String failedToRestartMessage = String.format("{\"content\": \"Failed to restart server %s\"}", serverLivenessChecker);
                    this.discordWebhook.sendDiscordMessage(failedToRestartMessage);
                }
            } else {
                System.out.printf("Server liveness checked failed for %s, sleeping for %d seconds\n", serverLivenessChecker, BACKOFF_IN_SECONDS);
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
            System.out.printf("Server: %s was available\n", serverLivenessChecker);
        }
    }
}
