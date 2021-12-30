package com.dino.metrics4j;

public enum MetricType {

    Store(0),
    Counter(1),
    Timer(2),
    Rate(3),
    ;

    private int id;

    MetricType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

}
