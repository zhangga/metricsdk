package com.dino.metrics4j;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TestMetrics {

    private Map<String, byte[]> heaps = new ConcurrentHashMap<>();

    @Test
    public void testMetrics() {
        mallocHeap();
        JvmMetrics.initialize("com.dino.metrics", null, true, true, true, true);
        try {
            Thread.sleep(300 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void mallocHeap() {
        new Timer("mallocHeap-1").schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("malloc heap size: 1M.");
                byte[] bytes = new byte[1*1024*1024];
                heaps.put(String.valueOf(System.currentTimeMillis()), bytes);
            }
        }, 1000, 1000);
        new Timer("mallocHeap-2").schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("malloc heap size: 2M.");
                byte[] bytes = new byte[2*1024*1024];
                heaps.put(String.valueOf(System.currentTimeMillis()), bytes);
            }
        }, 100, 1500);
    }

    private void freeHeap() {
        new Timer("freeHeap-1").schedule(new TimerTask() {
            @Override
            public void run() {
                heaps.keySet().forEach(k -> heaps.remove(k));
                System.gc();
            }
        }, 1000, 10000);
    }

}
