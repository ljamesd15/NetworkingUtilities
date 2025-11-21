package com.personal.networkingUtilities.utils.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CloudwatchMetricEmitter implements MetricEmitter {

    private final CloudWatchClient cloudWatchClient;

    private static final Logger logger = LoggerFactory.getLogger(CloudwatchMetricEmitter.class);

    public CloudwatchMetricEmitter(final CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public boolean emitMetric(String namespace, String dimensionName, String dimensionValue, String metricName, double dataPoint) {
        try {
            Dimension dimension = Dimension.builder()
                    .name(dimensionName)
                    .value(dimensionValue)
                    .build();

            String time = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
            Instant instant = Instant.parse(time);

            MetricDatum datum = MetricDatum.builder()
                    .metricName(metricName)
                    .unit(StandardUnit.NONE)
                    .value(dataPoint)
                    .timestamp(instant)
                    .dimensions(dimension).build();

            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace(namespace)
                    .metricData(datum).build();

            this.cloudWatchClient.putMetricData(request);

        } catch (CloudWatchException ex) {
            logger.error("Unable to emit metric: {}: {}={} metric={} value={}", namespace, dimensionName, dimensionValue, metricName, dataPoint, ex);
            return false;
        }

        logger.info("Emitted metric: {}: {}={} metric={} value={}", namespace, dimensionName, dimensionValue, metricName, dataPoint);
        return true;
    }
}
