package org.networkingUtilities.serverHealth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

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

    public boolean isServerAvailable() {
        try (final Socket socket = SSLSocketFactory.getDefault().createSocket(this.hostname, this.port)) {
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


    @SuppressFBWarnings
    public boolean restartServer() {
        if (serverRestartFilePath.isEmpty()) {
            System.out.println("No server restart file defined");
            return false;
        }
        try {
            Runtime.getRuntime().exec(String.format("cmd /c start \"\" %s", this.serverRestartFilePath.get()));
            return true;
        } catch (IOException ex) {
            System.out.println("Unable to restart server due to exception");
            ex.printStackTrace();
            return false;
        }
    }
}
