package com.personal.networkingUtilities.utils.outputter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.networkingUtilities.model.WebhookSecret;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.io.IOException;

public class SlackWebhook implements Outputter {

    private static final String SLACK_SECRET_ARN =
            "arn:aws:secretsmanager:us-west-2:872167319659:secret:prod/NetworkUtilities/SlackWebhook-zZxYQu";

    private final SecretsManagerClient secretsManagerClient;
    private final Slack slack;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhook.class);

    public SlackWebhook(final SecretsManagerClient secretsManagerClient,
                        final Slack slack,
                        final ObjectMapper objectMapper) {
        this.secretsManagerClient = secretsManagerClient;
        this.slack = slack;
        this.objectMapper = objectMapper;
    }


    public boolean sendMessage(final String jsonContent) {
        final String webhookUrl = this.getWebhookUrl(SLACK_SECRET_ARN);
        Payload payload = Payload.builder()
                .text(jsonContent)
                .build();
        try {
            final WebhookResponse response = this.slack.send(webhookUrl, payload);
            return response.getCode() == 200;
        } catch (IOException ex) {
            return false;
        }
    }

    private String getWebhookUrl(final String secretName) {
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
