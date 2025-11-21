package com.personal.networkingUtilities.serverHealth;

import com.ibasco.agql.core.enums.RateLimitType;
import com.ibasco.agql.core.util.FailsafeOptions;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions;
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import javax.net.ssl.SSLSocketFactory;

@Builder
@Data
public class ServerHealthClient {

    private final String hostname;
    private final int port;
    private final Optional<String> maybeServerRestartFilePath;
    private final Optional<ServerType> maybeServerType;

    private static final Logger logger = LoggerFactory.getLogger(ServerHealthClient.class);

    public enum ServerType {
        STEAM,
        UNKNOWN
    }

    public boolean isServerAvailable() {
        ServerType serverType = maybeServerType.orElse(ServerType.UNKNOWN);

        switch (serverType) {
            case STEAM -> {
                return checkSteamServer(this.hostname, this.port);
            }
            // Handles UNKNOWN too
            default -> {
                return checkTcpConnection(this.hostname, this.port);
            }
        }
    }

    private boolean checkSteamServer(String hostname, int port) {
        SourceQueryOptions queryOptions = SourceQueryOptions.builder()
                .option(FailsafeOptions.FAILSAFE_RATELIMIT_TYPE, RateLimitType.BURST)
                .build();

        try (SourceQueryClient client = new SourceQueryClient(queryOptions)) {
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            SourceServer info = client.getInfo(address).join().getResult();
            logger.info("Received response: Game={} WorldName={} at {}:{}", info.getGameId(), info.getName(), info.getHostAddress(), info.getGamePort());
            return true;
        } catch (IOException | CompletionException ex) {
            logger.error("Error connecting to Steam server: {}:{}", hostname, port, ex);
            return false;
        }
    }

    private boolean checkTcpConnection(String hostname, int port) {
        try (final Socket socket = SSLSocketFactory.getDefault().createSocket(this.hostname, this.port)) {
            return socket.isConnected();
        } catch (IOException ex) {
            logger.info("Error checking server availability", ex);
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%d", this.hostname, this.port);
    }


    public boolean restartServer() {
        if (this.maybeServerRestartFilePath.isEmpty()) {
            logger.warn("No server restart file defined");
            return false;
        }
        try {
            Runtime.getRuntime().exec(String.format("cmd /c start \"\" %s", this.maybeServerRestartFilePath.get()));
            return true;
        } catch (IOException ex) {
            logger.error("Unable to restart server due to exception", ex);
            return false;
        }
    }
}
