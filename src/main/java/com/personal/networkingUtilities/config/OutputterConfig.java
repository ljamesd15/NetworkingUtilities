package com.personal.networkingUtilities.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.networkingUtilities.utils.outputter.DiscordWebhook;
import com.personal.networkingUtilities.utils.outputter.Outputter;
import com.personal.networkingUtilities.utils.outputter.SlackWebhook;
import com.slack.api.Slack;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import javax.inject.Named;

@Module
public class OutputterConfig {

    @Provides
    @Named("Discord")
    public Outputter getDiscordOutputter(@Named("SecretsFetcher") final SecretsManagerClient secretsManagerClient,
                                         ObjectMapper objectMapper) {
        return new DiscordWebhook(secretsManagerClient, objectMapper);
    }

    @Provides
    @Named("Slack")
    public Outputter getSlackOutputter(@Named("SecretsFetcher") final SecretsManagerClient secretsManagerClient,
                                         Slack slack,
                                         ObjectMapper objectMapper) {
        return new SlackWebhook(secretsManagerClient, slack, objectMapper);
    }
}
