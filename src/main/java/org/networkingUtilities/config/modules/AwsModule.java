package org.networkingUtilities.config.modules;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import javax.inject.Named;

@Module
public class AwsModule {

    @Provides
    @Named("Default")
    public Region getDefaultAwsRegion() throws RuntimeException {
        return Region.US_WEST_2;
    }

    @Provides
    @Named("PartitionHome")
    public Region getPartitionHomeAwsRegion() {
        // Some services like Route53 and IAM operate from within a single region per partition so we need to hit the correct regional endpoint
        return Region.AWS_GLOBAL;
    }

    @Provides
    @Named("DynamicDns")
    public AwsCredentialsProvider getDynamicDnsCredsProvider() {
        return DefaultCredentialsProvider.builder()
                .profileName("dynamic-dns")
                .build();
    }

    @Provides
    @Named("SecretsFetcher")
    public AwsCredentialsProvider getSecretsFetcherCredsProvider() {
        return DefaultCredentialsProvider.builder()
                .profileName("secrets-fetcher")
                .build();
    }

    @Provides
    @Named("DynamicDns")
    public Route53Client getDynamicDnsRoute53Client(@Named("PartitionHome") final Region region,
                                                    @Named("DynamicDns") final AwsCredentialsProvider credentialsProvider) {
        return Route53Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Named("SecretsFetcher")
    public SecretsManagerClient getSecretsFetcherSecretsManagerClient(@Named("Default") final Region region,
                                                                      @Named("SecretsFetcher") final AwsCredentialsProvider credentialsProvider) {
        return SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
