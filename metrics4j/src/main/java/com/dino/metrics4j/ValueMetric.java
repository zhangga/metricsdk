package com.dino.metrics4j;

@FunctionalInterface
public interface ValueMetric<T> {

    public T getValue();

}
