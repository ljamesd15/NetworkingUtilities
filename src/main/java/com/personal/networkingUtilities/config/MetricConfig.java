package com.personal.networkingUtilities.config;

import com.personal.networkingUtilities.utils.metrics.CloudwatchMetricEmitter;
import com.personal.networkingUtilities.utils.metrics.MetricEmitter;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

import javax.inject.Named;

@Module
public class MetricConfig {

    @Provides
    @Named("Cloudwatch")
    public MetricEmitter getCloudwatchMetricEmitter(@Named("CloudwatchReadWrite") final CloudWatchClient cloudWatchClient) {
        return new CloudwatchMetricEmitter(cloudWatchClient);
    }
}
