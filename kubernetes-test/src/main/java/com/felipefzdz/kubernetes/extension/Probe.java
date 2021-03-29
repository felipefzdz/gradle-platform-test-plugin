package com.felipefzdz.kubernetes.extension;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class Probe {

    private final Property<Integer> retries;

    private final Property<Integer> delay;

    private final Property<Integer> port;

    private final Property<Integer> status;

    private final Property<String> path;

    @Inject
    public Probe(ObjectFactory objects) {
        this.retries = objects.property(Integer.class);
        this.delay = objects.property(Integer.class);
        this.port = objects.property(Integer.class);
        this.status = objects.property(Integer.class);
        this.path = objects.property(String.class);
    }


    public Property<Integer> getRetries() {
        return retries;
    }

    public Property<Integer> getDelay() {
        return delay;
    }

    public Property<Integer> getPort() {
        return port;
    }

    public Property<Integer> getStatus() {
        return status;
    }

    public Property<String> getPath() {
        return path;
    }
}
