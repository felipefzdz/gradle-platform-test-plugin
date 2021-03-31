package com.felipefzdz.kubernetes.extension;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class Deployment {
    private FileCollection manifests;
    private final RegularFileProperty script;
    private final Chart chart;
    private final Edge edge;

    @Inject
    public Deployment(ObjectFactory objects) {
        this.script = objects.fileProperty();
        this.edge = objects.newInstance(Edge.class);
        this.chart = objects.newInstance(Chart.class);
    }

    public FileCollection getManifests() {
        return manifests;
    }

    public void setManifests(FileCollection manifests) {
        this.manifests = manifests;
    }

    public RegularFileProperty getScript() {
        return script;
    }

    public Edge getEdge() {
        return edge;
    }

    public Chart getChart() {
        return chart;
    }

    public void edge(Action<? super Edge> action) {
        action.execute(edge);
    }

    public void chart(Action<? super Chart> action) {
        action.execute(chart);
    }
}
