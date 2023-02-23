package org.networkingUtilities.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.Builder;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Builder
public class DiscordWebhook {

    private static final String DEFAULT_WEBHOOK_URL =
            "https://discordapp.com/api/webhooks/1077964961105596426/_heFLKBXbX1WW0g53YjlrmRElWV6Go9-4OwQJlHyWPlCIxjGE-xUXZEHo0dPILtJJ0UJ";

    @Builder.Default
    private final String webhookUrl = DEFAULT_WEBHOOK_URL;


    public boolean sendDiscordMessage(final String jsonContent) {
        final HttpPost httpPost = new HttpPost(this.webhookUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            httpPost.setEntity(new StringEntity(jsonContent));
            final CloseableHttpResponse response = client.execute(httpPost);
            System.out.printf("%s %s%n", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException ex) {
            System.out.printf("Error sending request to %s%n", this.webhookUrl);
            ex.printStackTrace();
        }
        return false;
    }
}
