package com.dino.metrics4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MetricsClient {

    private static final String defaultServerHost = "127.0.0.1";
    private static final int defaultServerPort = 9110;

    private static DatagramSocket client = null;

    private static void initDefaultClient() {
        try {
            SocketAddress address = new InetSocketAddress(defaultServerHost, defaultServerPort);
            client = new DatagramSocket();
            client.setSoTimeout(1000);
            client.connect(address);
        } catch (Exception e) {
            client = null;
            e.printStackTrace();
        }
    }

    protected static void initClient(String serverHost, int serverPort) {
        try {
            SocketAddress address = new InetSocketAddress(serverHost, serverPort);
            client = new DatagramSocket();
            client.setSoTimeout(1000);
            client.connect(address);
        } catch (Exception e) {
            client = null;
            e.printStackTrace();
        }
    }

    private static DatagramSocket getClient() {
        if (client == null) {
            initDefaultClient();
        }
        return client;
    }

    protected static void emitCounterMetrics(String name, String tags, Object value) {
        if (getClient() == null)
            return;
        String metrics = MetricsUtils.getMetricValue(name, tags, value);
        send(metrics);
    }

    protected static void emitTimerMetrics(String name, String tags, double time) {
        if (getClient() == null)
            return;
    }

    private static void send(String metrics) {
        try {
            byte[] data = metrics.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, 0, data.length);
            getClient().send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
