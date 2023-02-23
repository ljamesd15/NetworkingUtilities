package org.networkingUtilities.localServer.serverChecker;

import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

@Builder
@Data
public class ServerLivenessChecker {

    private final String hostname;
    private final int port;
    private final Optional<String> serverRestartFilePath;

    public boolean isServerAvailable() {
        try (final Socket socket = new Socket(this.hostname, this.port)) {
            return socket.isConnected();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%d", this.hostname, this.port);
    }

    public boolean restartServer() {
        if (serverRestartFilePath.isEmpty()) {
            System.out.println("No server restart file defined");
            return false;
        }
        try {
            Runtime.
                    getRuntime().
                    exec(String.format("cmd /c start \"\" %s", this.serverRestartFilePath.get()));
            return true;
        } catch (IOException ex) {
            System.out.println("Unable to restart server due to exception");
            ex.printStackTrace();
            return false;
        }
    }
}
