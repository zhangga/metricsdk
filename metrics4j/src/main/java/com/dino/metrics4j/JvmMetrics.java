package com.dino.metrics4j;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.*;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class JvmMetrics {

    /**
     * 初始化JVM监控
     * @param serviceIdentify 服务的唯一标识
     * @param tags 可以为空，用来区分不同的JVM实例
     * @param bufferPool
     * @param threadStates
     * @param garbageCollector
     * @param memoryUsage
     */
    public static void initialize(String serviceIdentify, Map<String, String> tags, boolean bufferPool,
                                  boolean threadStates, boolean garbageCollector, boolean memoryUsage) {
        String prefix = MetricsUtils.name(serviceIdentify, "jvm");
        MetricsRegistry registry = MetricsUtils.getDefaultRegistry();
        String tagString = null;
        // 添加默认的tags
        if (tags == null) {
            tags = new HashMap<>();
            try {
                String host = InetAddress.getLocalHost().getHostAddress();
                tags.put("node", host);
                tagString = MetricsUtils.getTagKVs(tags);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            tagString = MetricsUtils.getTagKVs(tags);
        }

        // buffer
        if (bufferPool) {
            BufferPoolMetricSet bufferPoolMetricSet = new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer());
            Map<String, Metric> metricMap = bufferPoolMetricSet.getMetrics();
            // 共有6个key
            // direct/mapped + count/used/capacity
            // 输出: ${serviceIdentify}.jvm.buffer.direct.used
            for (Map.Entry<String, Metric> entry : metricMap.entrySet()) {
                String name = MetricsUtils.name(prefix, "buffer", entry.getKey());
                registry.addMetricTag(name, tagString);
                registry.gauge(name, () -> {
                    JmxAttributeGauge gauge = (JmxAttributeGauge) entry.getValue();
                    return gauge.getValue();
                });
            }
        }

        // thread
        if (threadStates) {
            // 因为数据上报间隔本身就有10秒, 所以这里没有使用CachedThreadStatesGaugeSet
            ThreadStatesGaugeSet cachedThreadStatesGaugeSet = new ThreadStatesGaugeSet();
            Map<String, Metric> metricMap = cachedThreadStatesGaugeSet.getMetrics();
            // 共有10个key
            // terminated/new/timed_waiting/blocked/waiting/daemon/runnable/deadlock.count, deadlocks, count
            // 输出: ${serviceIdentify}.jvm.threads.runnable.count
            for (Map.Entry<String, Metric> entry : metricMap.entrySet()) {
                // deadlocks中记录的是死锁状态, 不上报metrics
                if (entry.getKey().equals("deadlocks"))
                    continue;
                String name = MetricsUtils.name(prefix, "threads", entry.getKey());
                registry.addMetricTag(name, tagString);
                registry.gauge(name, () -> {
                    Gauge gauge = (Gauge) entry.getValue();
                    return gauge.getValue();
                });
            }
        }

        // GC
        if (garbageCollector) {
            GarbageCollectorMetricSet garbageCollectorMetricSet = new GarbageCollectorMetricSet();
            Map<String, Metric> metricMap = garbageCollectorMetricSet.getMetrics();
            // G1-Old-Generation/G1-Young-Generation + count/time (仅在使用G1垃圾收集器时)
            // 输出: ${serviceIdentify}.jvm.gc.g1.time
            for (Map.Entry<String, Metric> entry : metricMap.entrySet()) {
                String name = MetricsUtils.name(prefix, "gc", entry.getKey());
                registry.addMetricTag(name, tagString);
                registry.gauge(name, () -> {
                    Gauge gauge = (Gauge) entry.getValue();
                    return gauge.getValue();
                });
            }
        }

        // memory
        if (memoryUsage) {
            MemoryUsageGaugeSet memoryUsageGaugeSet = new MemoryUsageGaugeSet();
            Map<String, Metric> metricMap = memoryUsageGaugeSet.getMetrics();
            // heap/non-heap/G1/totaol + committed/init/max/usage/used/used-after-gc
            // 输出: ${serviceIdentify}.jvm.mem.heap.used
            for (Map.Entry<String, Metric> entry : metricMap.entrySet()) {
                // CodeHeap中记录的是代码堆数据? 不上报metrics
                if (entry.getKey().startsWith("pools.CodeHeap-"))
                    continue;
                // Compressed-Class-Space中记录的是压缩的类空间? 不上报metrics
                if (entry.getKey().startsWith("pools.Compressed-Class-Space"))
                    continue;
                // CodeHeap中记录的是元数据信息? 不上报metrics
                if (entry.getKey().startsWith("pools.Metaspace"))
                    continue;
                String name = MetricsUtils.name(prefix, "mem", entry.getKey());
                registry.addMetricTag(name, tagString);
                registry.gauge(name, () -> {
                    Gauge gauge = (Gauge) entry.getValue();
                    return gauge.getValue();
                });
            }
        }
    }

}
