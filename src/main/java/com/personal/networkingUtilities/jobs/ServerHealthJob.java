package org.networkingUtilities.jobs;

import org.networkingUtilities.serverHealth.ServerHealthClient;
import org.networkingUtilities.utils.outputter.Outputter;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import static org.networkingUtilities.jobs.JobRunner.BACKOFF_IN_SECONDS;
import static org.networkingUtilities.jobs.JobRunner.MAX_RETRIES;

public class ServerHealthJob implements BaseJob {

    private final Outputter outputter;

    @Inject
    public ServerHealthJob(final Outputter outputter) {
        this.outputter = outputter;
    }

    @Override
    public boolean runJob(final List<String> arguments) {
        if (arguments.size() < 2) {
            System.out.println("Insufficient arguments. You must provide at least the hostname and the port of the server");
            return false;
        }
        final String hostname = arguments.get(0);
        int port;
        try {
            port = Integer.parseInt(arguments.get(1));
        } catch (NumberFormatException ex) {
            System.out.printf("Unable to parse integer from %s%n", arguments.get(1));
            return false;
        }
        final Optional<String> maybeServerRestartFile =
                Optional.ofNullable(arguments.size() > 2 ? arguments.get(2) : null);
        final ServerHealthClient serverHealthClient = ServerHealthClient.builder()
                .hostname(hostname)
                .port(port)
                .serverRestartFilePath(maybeServerRestartFile)
                .build();
        this.checkServerLiveness(serverHealthClient, MAX_RETRIES);
        return true;
    }

    private void checkServerLiveness(final ServerHealthClient serverHealthClient, final int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException(String.format("Retries %d, cannot be less than 0%n", retries));
        }


        if (!serverHealthClient.isServerAvailable()) {
            if (retries == 0) {
                final String failureMessage =
                        String.format("{\"content\": \"Server %s is unavailable, attempting server restart\"}", serverHealthClient);
                this.outputter.sendMessage(failureMessage);
                final boolean restartedSuccessfully = serverHealthClient.restartServer();
                if (!restartedSuccessfully) {
                    final String failedToRestartMessage =
                            String.format("{\"content\": \"Failed to restart server %s\"}", serverHealthClient);
                    this.outputter.sendMessage(failedToRestartMessage);
                }
            } else {
                System.out.printf("Server liveness check failed for %s, sleeping for %d seconds%n",
                        serverHealthClient, BACKOFF_IN_SECONDS);
                try {
                    Thread.sleep(BACKOFF_IN_SECONDS * 1000);
                } catch (InterruptedException ex) {
                    System.out.println("Error while sleeping after failed server connection. Failing job run");
                    ex.printStackTrace();
                    return;
                }
                checkServerLiveness(serverHealthClient, retries - 1);
            }
        } else {
            System.out.printf("Server: %s was available%n", serverHealthClient);
        }
    }
}
