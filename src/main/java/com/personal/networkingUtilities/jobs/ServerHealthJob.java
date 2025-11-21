package com.personal.networkingUtilities.jobs;

import com.personal.networkingUtilities.serverHealth.ServerHealthClient;
import com.personal.networkingUtilities.utils.metrics.MetricEmitter;
import com.personal.networkingUtilities.utils.outputter.Outputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.personal.networkingUtilities.jobs.JobRunner.BACKOFF_IN_SECONDS;
import static com.personal.networkingUtilities.jobs.JobRunner.MAX_RETRIES;

public class ServerHealthJob implements BaseJob {

    private static final String NAMESPACE = "HOMESERVERS";
    private static final String ADDRESS_DIMENSION = "Address";
    private static final String LIVENESS_METRIC_NAME = "Liveness";

    private final Outputter outputter;
    private final MetricEmitter metricEmitter;

    private static final Logger logger = LoggerFactory.getLogger(ServerHealthJob.class);

    public ServerHealthJob(final Outputter outputter, final MetricEmitter metricEmitter) {
        this.outputter = outputter;
        this.metricEmitter = metricEmitter;
    }

    @Override
    public boolean runJob(final List<String> arguments) {
        if (arguments.size() < 2) {
            logger.error("Insufficient arguments. You must provide at least the hostname and the port of the server");
            return false;
        }
        final String hostname = arguments.get(0);
        int port;
        try {
            port = Integer.parseInt(arguments.get(1));
        } catch (NumberFormatException ex) {
            logger.error("Unable to parse integer from {}", arguments.get(1));
            return false;
        }
        final Optional<String> maybeServerRestartFile =
                Optional.ofNullable(arguments.size() > 2 ? arguments.get(2) : null);
        final ServerHealthClient serverHealthClient = ServerHealthClient.builder()
                .hostname(hostname)
                .port(port)
                .serverRestartFilePath(maybeServerRestartFile)
                .build();

        boolean success = this.checkServerLiveness(serverHealthClient, MAX_RETRIES);
        this.metricEmitter.emitMetric(NAMESPACE,
                ADDRESS_DIMENSION,
                hostname,
                LIVENESS_METRIC_NAME,
                success ? 1: 0);

        return true;
    }

    private boolean checkServerLiveness(final ServerHealthClient serverHealthClient, final int retries) {
        if (retries < 0) {
            return false;
        }

        if (serverHealthClient.isServerAvailable()) {
            logger.info("Server: {} was available", serverHealthClient);
            return true;
        }

        final String failureMessage =
                String.format("Server %s is unavailable, attempting server restart", serverHealthClient);
        this.outputter.sendMessage(failureMessage);
        final boolean restartedSuccessfully = serverHealthClient.restartServer();

        if (!restartedSuccessfully) {
            final String failedToRestartMessage =
                    String.format("Failed to restart server %s", serverHealthClient);
            this.outputter.sendMessage(failedToRestartMessage);
            return false;
        }

        if (retries == 0) {
            this.outputter.sendMessage("Exhausted retries and could not bring up server. Giving up.");
            return false;
        } else {
            logger.error("Server liveness check failed for {}, sleeping for {} seconds",
                    serverHealthClient,
                    BACKOFF_IN_SECONDS);
            try {
                Thread.sleep(BACKOFF_IN_SECONDS * 1000);
            } catch (InterruptedException ex) {
                logger.error("Error while sleeping after failed server connection. Failing job run", ex);
                return false;
            }
            return this.checkServerLiveness(serverHealthClient, retries - 1);
        }
    }
}
