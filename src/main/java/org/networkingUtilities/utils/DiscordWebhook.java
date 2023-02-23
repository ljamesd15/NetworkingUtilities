package org.networkingUtilities.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final String webhookUrl;

    public DiscordWebhook(final String webhookUrl) {
        this.webhookUrl = webhookUrl;
    };

    public boolean sendDiscordMessage(final String jsonContent) {
        try {
            final URL url = new URL(this.webhookUrl);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");

            final byte[] out = jsonContent.getBytes(StandardCharsets.UTF_8);
            final OutputStream stream = connection.getOutputStream();
            stream.write(out);

            System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());
            connection.disconnect();

            return true;
        } catch (IOException ex) {
            System.out.printf("Error sending request to %s\n", this.webhookUrl);
            ex.printStackTrace();
            return false;
        }
    }
}
