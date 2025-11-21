package com.personal.networkingUtilities.utils.metrics;

public interface MetricEmitter {

    boolean emitMetric(String namespace, String dimensionName, String dimensionValue, String metricName, double dataPoint);
}
