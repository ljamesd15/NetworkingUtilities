package com.personal.networkingUtilities.jobs;

import com.personal.networkingUtilities.serverHealth.ServerHealthClient;
import com.personal.networkingUtilities.utils.Arguments;
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
    public boolean runJob(final Arguments arguments) {
        final Optional<String> maybeHostname = arguments.getArgumentValue(Arguments.HOSTNAME_ARG);
        final Optional<String> maybePort = arguments.getArgumentValue(Arguments.PORT_ARG);

        if (maybeHostname.isEmpty() || maybePort.isEmpty()) {
            logger.error("Missing arguments. Either hostname or port is missing");
            return false;
        }

        int port;
        try {
            port = Integer.parseInt(maybePort.get());
        } catch (NumberFormatException ex) {
            logger.error("Unable to parse integer from {}", maybePort.get());
            return false;
        }

        final Optional<String> maybeServerRestartFile = arguments.getArgumentValue(Arguments.SERVER_RESTART_FILE_PATH_ARG);
        final Optional<ServerHealthClient.ServerType> maybeServerType = arguments.getArgumentValue(Arguments.SERVER_TYPE_ARG)
                .map(ServerHealthClient.ServerType::valueOf);
        final ServerHealthClient serverHealthClient = ServerHealthClient.builder()
                .hostname(maybeHostname.get())
                .port(port)
                .maybeServerRestartFilePath(maybeServerRestartFile)
                .maybeServerType(maybeServerType)
                .build();

        boolean success = this.checkServerLiveness(serverHealthClient, MAX_RETRIES);
        this.metricEmitter.emitMetric(NAMESPACE,
                ADDRESS_DIMENSION,
                maybeHostname.get(),
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
