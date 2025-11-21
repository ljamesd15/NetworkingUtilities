package com.personal.networkingUtilities.utils.outputter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.networkingUtilities.model.WebhookSecret;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

public class DiscordWebhook implements Outputter {

    private static final String DISCORD_SECRET_ARN =
            "arn:aws:secretsmanager:us-west-2:872167319659:secret:prod/NetworkUtilities/DiscordWebhook-gDc78L";

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhook.class);

    @Inject
    public DiscordWebhook(@Named("SecretsFetcher") final SecretsManagerClient secretsManagerClient, ObjectMapper objectMapper) {
        this.secretsManagerClient = secretsManagerClient;
        this.objectMapper = objectMapper;
    }


    public boolean sendMessage(final String jsonContent) {
        final HttpPost httpPost = new HttpPost(this.getWebhookUrl(DISCORD_SECRET_ARN));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            httpPost.setEntity(new StringEntity(jsonContent));
            final CloseableHttpResponse response = client.execute(httpPost);
            logger.info("{} {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException ex) {
            logger.error("Error sending request", ex);
        }
        return false;
    }

    public String getWebhookUrl(final String secretName) {
        try {
            final GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            final String stringifiedWebhook = this.secretsManagerClient.getSecretValue(valueRequest).secretString();
            final WebhookSecret webhookSecret = this.objectMapper.readValue(stringifiedWebhook, WebhookSecret.class);
            return webhookSecret.webhookUrl();
        } catch (SdkException ex) {
            throw new RuntimeException(String.format("Unable to fetch secret: %s", secretName), ex);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(String.format("Unable to parse secret: %s", secretName), ex);
        }
    }
}
