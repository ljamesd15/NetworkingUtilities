package org.networkingUtilities.utils.outputter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

public class DiscordWebhook implements Outputter {

    private static final String DISCORD_SECRET_ARN =
            "arn:aws:secretsmanager:us-west-2:872167319659:secret:prod/NetworkUtilities/DiscordWebhook-DKDoeN";

    @SuppressFBWarnings("EI_EXPOSE_REP")
    private final SecretsManagerClient secretsManagerClient;

    @Inject
    public DiscordWebhook(@Named("SecretsFetcher") final SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }


    public boolean sendMessage(final String jsonContent) {
        final HttpPost httpPost = new HttpPost(this.getWebhookUrl());
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            httpPost.setEntity(new StringEntity(jsonContent));
            final CloseableHttpResponse response = client.execute(httpPost);
            System.out.printf("%s %s%n", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException ex) {
            System.out.printf("Error sending request%n");
            ex.printStackTrace();
        }
        return false;
    }

    private String getWebhookUrl() {
        final GetSecretValueResponse response = this.secretsManagerClient.getSecretValue(GetSecretValueRequest.builder()
                .secretId(DISCORD_SECRET_ARN)
                .build());
        return response.secretString();
    }
}
