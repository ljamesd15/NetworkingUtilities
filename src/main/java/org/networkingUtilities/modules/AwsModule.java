package org.networkingUtilities.modules;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;

@Module
public class AwsModule {

    @Provides
    @Named("AWSRegion")
    public Region getAwsRegion() {
        return Region.US_WEST_2;
    }

    @Provides
    @Named("DynamicDns")
    public AwsCredentialsProvider getDefaultCredsProvider() {
        return DefaultCredentialsProvider.builder()
                .profileName("dynamic-dns")
                .build();
    }

    @Provides
    @Named("DynamicDns")
    public Route53Client getDefaultRoute53Client(@Named("AWSRegion") final Region region,
                                                 @Named("DynamicDns") final AwsCredentialsProvider credentialsProvider) {
        return Route53Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }
}
