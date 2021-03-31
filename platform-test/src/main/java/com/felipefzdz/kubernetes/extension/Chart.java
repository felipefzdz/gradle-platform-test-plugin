package com.felipefzdz.kubernetes.extension;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class Chart {

    private final Property<String> name;
    private final Property<String> version;
    private final Property<String> release;
    private final Property<String> user;
    private final Property<String> password;
    private final Property<String> repo;


    @Inject
    public Chart(ObjectFactory objects) {
        this.name = objects.property(String.class);
        this.version = objects.property(String.class);
        this.release = objects.property(String.class);
        this.user = objects.property(String.class);
        this.password = objects.property(String.class);
        this.repo = objects.property(String.class);
    }

    public Property<String> getName() {
        return name;
    }

    public Property<String> getVersion() {
        return version;
    }

    public Property<String> getRelease() {
        return release;
    }

    public Property<String> getUser() {
        return user;
    }

    public Property<String> getPassword() {
        return password;
    }

    public Property<String> getRepo() {
        return repo;
    }
}
