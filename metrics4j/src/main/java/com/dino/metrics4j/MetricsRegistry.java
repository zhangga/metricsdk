package com.dino.metrics4j;

import java.util.Map;
import java.util.concurrent.*;

public class MetricsRegistry {

    private static final MetricsRegistry defaultRegistry = new MetricsRegistry();

    private Map<String, ValueMetric> metrics = new ConcurrentHashMap<>();
    private Map<String, String> metricTags = new ConcurrentHashMap<>();

    private MetricsRegistry() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(()-> {
            for (Map.Entry<String, ValueMetric> entry : metrics.entrySet()) {
                MetricsUtils.emitStoreMetrics(entry.getKey(), metricTags.get(entry.getKey()), entry.getValue().getValue());
            }
        }, 100, 1000, TimeUnit.MILLISECONDS);
    }

    protected static MetricsRegistry getDefaultRegistry() {
        return defaultRegistry;
    }

    public void addMetricTag(String key, String tag) {
        metricTags.put(key, tag);
    }

    public void gauge(String key, ValueMetric metric) {
        metrics.put(key, metric);
    }

}
