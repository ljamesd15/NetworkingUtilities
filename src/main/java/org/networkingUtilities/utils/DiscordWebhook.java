package org.networkingUtilities.utils;

import lombok.Builder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Builder
public class DiscordWebhook {

    private static final String DEFAULT_WEBHOOK_URL =
            "https://discordapp.com/api/webhooks/1077964961105596426/_heFLKBXbX1WW0g53YjlrmRElWV6Go9-4OwQJlHyWPlCIxjGE-xUXZEHo0dPILtJJ0UJ";

    @Builder.Default
    private final String webhookUrl = DEFAULT_WEBHOOK_URL;

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
