package com.dino.metrics4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class MetricsUtils {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static MetricsRegistry getDefaultRegistry() {
        return MetricsRegistry.getDefaultRegistry();
    }

    public static void initMetricsClient(String serverHost, int serverPort) {
        MetricsClient.initClient(serverHost, serverPort);
    }

    /**
     * 累计计数型
     * @param name
     * @param tags
     * @param count
     */
    public static void emitCounterMetrics(String name, String tags, Object count) {
        MetricsClient.emitCounterMetrics(name, tags, count);
    }

    /**
     *
     * @param name
     * @param tags
     * @param time
     */
    public static void emitTimerMetrics(String name, String tags, double time) {
        MetricsClient.emitTimerMetrics(name, tags, time);
    }

    public static String getTagKVs(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            String json = JSON_MAPPER.writeValueAsString(tags);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMetricValue(String metricName, String tags, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append(metricName).append("$");
        if (tags != null && !tags.isEmpty()) {
            sb.append(tags).append("$");
        }
        sb.append(value);
        return sb.toString();
    }

    public static String name(String... keys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length - 1; i++) {
            if (keys[i] == null || keys[i].length() == 0)
                continue;
            sb.append(keys[i]).append(".");
        }
        if (keys[keys.length - 1] != null && keys[keys.length - 1].length() > 0) {
            sb.append(keys[keys.length - 1]);
        }
        return sb.toString();
    }

}
