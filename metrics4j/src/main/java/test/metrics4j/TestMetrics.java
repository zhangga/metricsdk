package test.metrics4j;

import com.dino.metrics4j.JvmMetrics;
import com.dino.metrics4j.MetricsUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// 测试监控类
public class TestMetrics {

    private static final String SERVICE_IDENTIFY = "com.dino.metrics";

    private static Map<Long, TestObj> heaps = new ConcurrentHashMap<>();

    private static AtomicLong id = new AtomicLong(0);

    public static void main(String[] args) {
        JvmMetrics.initialize(SERVICE_IDENTIFY, null, true, true, true, true);
        monitorJVM();
        mallocHeap();
        freeHeap();
        scheduleThread();
    }

    private static void monitorJVM() {
        new Timer("monitorJVM-1").schedule(new TimerTask() {
            @Override
            public void run() {
                long heapMax = Runtime.getRuntime().maxMemory()/1024/1024;
                long heapFree = Runtime.getRuntime().freeMemory()/1024/1024;
                int processors = Runtime.getRuntime().availableProcessors();
                StringBuilder sb = new StringBuilder();
                sb.append("=======================================").append("\n");
                sb.append("Heap Max: ").append(heapMax).append("M, ").append("Heap Free: ").append(heapFree)
                        .append("M, processors: ").append(processors).append("\n");
                sb.append("=======================================").append("\n");
                System.out.println(sb.toString());
            }
        }, 1000, 5000);
    }

    private static void mallocHeap() {
        new Timer("mallocHeap-1").schedule(new TimerTask() {
            @Override
            public void run() {
                int mem = 5;
                System.out.println("malloc heap size: "+mem+"M.");
                TestObj obj = new TestObj();
                obj.setId(id.incrementAndGet());
                obj.setData(new byte[mem*1024*1024]);
                heaps.put(obj.getId(), obj);
                MetricsUtils.emitCounterMetrics(MetricsUtils.name(SERVICE_IDENTIFY, "ObjectMalloc"), null, 1);
            }
        }, 1000, 1000);
        new Timer("mallocHeap-2").schedule(new TimerTask() {
            @Override
            public void run() {
                long heapFree = Runtime.getRuntime().freeMemory()/1024/1024;
                int mem = 10;
                int count = (int) (heapFree / 4 / mem);
                for (int i = 0; i < count; i++) {
                    System.out.println("malloc heap size: "+mem+"M.");
                    TestObj obj = new TestObj();
                    obj.setId(id.incrementAndGet());
                    obj.setData(new byte[mem*1024*1024]);
                    heaps.put(obj.getId(), obj);
                    MetricsUtils.emitCounterMetrics(MetricsUtils.name(SERVICE_IDENTIFY, "ObjectMalloc"), null, 1);
                }
            }
        }, 1000, 3000);
    }

    private static void scheduleThread() {
        new Timer("scheduleThread-boss").schedule(new TimerTask() {
            @Override
            public void run() {
                int count = new Random().nextInt(5);
                for (int i = 0; i < count; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(new Random().nextInt(5*1000));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    MetricsUtils.emitCounterMetrics(MetricsUtils.name(SERVICE_IDENTIFY, "ScheduleThread"), null, 1);
                }
            }
        }, 1000, 1000);
    }

    private static void freeHeap() {
        new Timer("freeHeap-1").schedule(new TimerTask() {
            @Override
            public void run() {
                int i = new Random().nextInt(2);
                heaps.keySet().forEach(k -> {
                    if (k % 2 == i) {
                        heaps.remove(k);
                        MetricsUtils.emitCounterMetrics(MetricsUtils.name(SERVICE_IDENTIFY, "ObjectFree"), null, 1);
                    }
                });
            }
        }, 1000, 2000);
    }

}
