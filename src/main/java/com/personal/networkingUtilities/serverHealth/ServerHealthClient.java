package com.personal.networkingUtilities.serverHealth;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import javax.net.ssl.SSLSocketFactory;

@Builder
@Data
public class ServerHealthClient {

    private final String hostname;
    private final int port;
    private final Optional<String> serverRestartFilePath;

    private static final Logger logger = LoggerFactory.getLogger(ServerHealthClient.class);

    public boolean isServerAvailable() {
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
        if (serverRestartFilePath.isEmpty()) {
            logger.warn("No server restart file defined");
            return false;
        }
        try {
            Runtime.getRuntime().exec(String.format("cmd /c start \"\" %s", this.serverRestartFilePath.get()));
            return true;
        } catch (IOException ex) {
            logger.error("Unable to restart server due to exception", ex);
            return false;
        }
    }
}
