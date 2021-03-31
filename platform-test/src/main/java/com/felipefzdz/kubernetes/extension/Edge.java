package com.felipefzdz.kubernetes.extension;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class Edge {

    private final Property<String> name;
    private final Property<Integer> port;


    @Inject
    public Edge(ObjectFactory objects) {
        this.name = objects.property(String.class);
        this.port = objects.property(Integer.class);
    }

    public Property<String> getName() {
        return name;
    }

    public Property<Integer> getPort() {
        return port;
    }
}
