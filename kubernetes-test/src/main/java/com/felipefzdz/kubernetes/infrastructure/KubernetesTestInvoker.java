package com.felipefzdz.kubernetes.infrastructure;

import com.felipefzdz.kubernetes.extension.Chart;
import com.felipefzdz.kubernetes.extension.Deployment;
import com.felipefzdz.kubernetes.extension.KubernetesTestExtension;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class KubernetesTestInvoker {

    private final Logger logger;
    private final KubernetesTestExtension extension;
    private final Shell shell;

    public KubernetesTestInvoker(KubernetesTestExtension extension) {
        this.logger = Logging.getLogger(KubernetesTestInvoker.class);
        this.extension = extension;
        this.shell = new Shell(logger, extension.getProjectDir());
    }

    public void setup() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            setupK3d();
            logger.info("K3d setup elapsed time: " + stopwatch);
            maybeCreateNamespace();
            deployDeployment();
            exposeService();
            executeProbe();
            logger.info("Deployment available elapsed time: " + stopwatch);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the Kubernetes setup", e);
        }
    }

    public void cleanup() {
        try {
            cleanupK3d();
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the Kubernetes cleanup", e);
        }
    }


    private void setupK3d() {
        final List<String> command = join(getK3dClusterCommand(extension.getK3dVersion()), "create", "-p", extension.getProbe().getPort().get() + ":80@loadbalancer", "test-k3d");
        shell.run(command);
    }

    private void cleanupK3d() {
        shell.run(join(getK3dClusterCommand(extension.getK3dVersion()), "delete", "test-k3d"));
    }

    private void maybeCreateNamespace() {
        final String namespace = extension.getNamespace().get();
        if (!"default".equals(namespace)) {
            shell.run(join(getKubectlCommand(), "create", "namespace", namespace));
        }
    }

    private void copyManifests() {
        extension.getDeployment().getManifests().getFiles().forEach(this::copyFile);
    }

    private void copyFile(File file) {
        shell.run(asList("docker", "cp", file.getAbsolutePath(), "k3d-test-k3d-server-0:/" + file.getName()));
    }

    private void copyFolder(File file) {
        shell.run(asList("docker", "cp", file.getAbsolutePath() + "/.", "k3d-test-k3d-server-0:/"));
    }

    private void downloadSupportBundle() {
        shell.run(asList("docker", "cp", "k3d-test-k3d-server-0:/support-bundle.tar", "support-bundle.tar"));
    }

    private void deployManifests() {
        extension.getDeployment().getManifests().getFiles().forEach(this::deployManifest);
    }

    private void exposeService() {
        try {
            String ingressContent = IOUtils.toString(KubernetesTestInvoker.class.getClassLoader().getResourceAsStream("ingress.yaml"), UTF_8);
            File ingressManifest = File.createTempFile("temp", "ingress.yaml");
            String ingressManifestInterpolated = ingressContent
                    .replace("name:", "name: " + extension.getDeployment().getEdge().getName().get())
                    .replace("number:", "number: " + extension.getDeployment().getEdge().getPort().get());
            FileUtils.writeStringToFile(ingressManifest, ingressManifestInterpolated, UTF_8);
            copyFile(ingressManifest);
            deployManifest(ingressManifest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deployManifest(File manifest) {
        shell.run(join(getKubectlWithNamespaceCommand(extension.getNamespace().get()), "apply", "-f", manifest.getName()));
    }

    private void executeScript(String script) {
        shell.run(asList("docker", "exec", "k3d-test-k3d-server-0", "sh", script));
    }

    private void executeScriptWithEnvVar(String script, String envVar) {
        shell.run(asList("docker", "exec", "-e", "NAMESPACE=" + envVar, "k3d-test-k3d-server-0", "sh", script));
    }

    private void executeProbe() {
        final HttpProbe.HttpProbeStatus result = HttpProbe.run(extension.getProbe());
        if (result.failure) {
            generateSupportBundle();
            throw new GradleException("Error while probing the application", result.maybeException.get());
        }
    }

    private void generateSupportBundle() {
        try {
            String supportBundleContent = IOUtils.toString(KubernetesTestInvoker.class.getClassLoader().getResourceAsStream("support_bundle.sh"), UTF_8);
            File supportBundleScript = File.createTempFile("temp", "support_bundle.sh");
            String supportBundleContentInterpolated = supportBundleContent.replace("K8S_NAMESPACE=", "K8S_NAMESPACE=" + extension.getNamespace().get());
            FileUtils.writeStringToFile(supportBundleScript, supportBundleContentInterpolated, UTF_8, true);
            copyFile(supportBundleScript);
            executeScript(supportBundleScript.getName());
            downloadSupportBundle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getK3dClusterCommand(String k3dVersion) {
        return asList("docker", "run", "--rm", "-v", "/var/run/docker.sock:/var/run/docker.sock", "rancher/k3d:" + k3dVersion, "cluster");
    }

    private List<String> getKubectlCommand() {
        return asList("docker", "exec", "k3d-test-k3d-server-0", "kubectl");
    }

    private List<String> getKubectlWithNamespaceCommand(String namespace) {
        return join(getKubectlCommand(), "--namespace", namespace);
    }

    private List<String> join(List<String> firstPartCommand, String... secondPartCommand) {
        return Stream.concat(firstPartCommand.stream(), Stream.of(secondPartCommand))
                .collect(Collectors.toList());
    }

    private void deployDeployment() {
        Deployer.of(extension.getDeployment(), this).deploy(extension);
    }

    private void installHelmChart(Chart chart) {
        try {
            File installChartScript = createInstallChartScript(chart);
            shell.run(asList("docker", "run", "-v", createKubeconfig().getAbsolutePath() + ":/root/.kube/config",
                    "-v", installChartScript.getAbsolutePath() + ":/config/" + installChartScript.getName(), "--rm",
                    "dtzar/helm-kubectl", "/bin/sh", "-c", "\"/config/" + installChartScript.getName() + "\""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private File createInstallChartScript(Chart chart) throws IOException {
        String installChartContent = IOUtils.toString(KubernetesTestInvoker.class.getClassLoader().getResourceAsStream("install_chart.sh"), UTF_8);
        File installChartScript = File.createTempFile("temp", "install_chart.sh");
        String installChartContentInterpolated = interpolateContent(ImmutableMap.of(
                "{release}", Optional.of(chart.getRelease().get()),
                "{name}", Optional.of(chart.getName().get()),
                "{version}", Optional.of(chart.getVersion().get()),
                "{repo}", Optional.of(chart.getRepo().get()),
                "http://host.docker.internal:8080", addToRepoCommand(chart)),
                installChartContent);
        FileUtils.writeStringToFile(installChartScript, installChartContentInterpolated, UTF_8, true);
        shell.run(asList("chmod", "+x", installChartScript.getAbsolutePath()));
        return installChartScript;
    }

    private Optional<String> addToRepoCommand(Chart chart) {
        if (chart.getUser().getOrNull() == null || chart.getPassword().getOrNull() == null) {
            return Optional.empty();
        }
        return Optional.of("http://host.docker.internal:8080 --username " + chart.getUser().get() + " --password " + chart.getPassword().get());
    }

    private String interpolateContent(Map<String, Optional<String>> interpolators, String content) {
        for (Map.Entry<String, Optional<String>> interpolator : interpolators.entrySet()) {
            if (interpolator.getValue().isPresent()) {
                content = content.replace(interpolator.getKey(), interpolator.getValue().get());
            }
        }
        return content;
    }

    @NotNull
    private File createKubeconfig() throws IOException {
        String kubeConfigContent = shell.run(asList("docker", "exec", "k3d-test-k3d-server-0", "kubectl", "config", "view", "--raw"));
        String k3sPort = shell.run(asList("docker", "port", "k3d-test-k3d-serverlb", "6443")).replace("0.0.0.0", "host.docker.internal");
        String kubeConfigContentInterpolated = kubeConfigContent.replace("127.0.0.1:6443", k3sPort);
        File kubeConfigScript = File.createTempFile("temp", "config");
        FileUtils.writeStringToFile(kubeConfigScript, kubeConfigContentInterpolated, UTF_8, true);
        return kubeConfigScript;
    }

    public interface Deployer {
        void deploy(KubernetesTestExtension extension);

        static Deployer of(Deployment deployment, KubernetesTestInvoker invoker) {
            if (deployment.getManifests() != null) {
                return new ManifestDeployer(invoker);
            }
            if (deployment.getScript().getOrNull() != null) {
                return new ScriptDeployer(invoker);
            }
            return new ChartDeployer(invoker);
        }
    }

    public static class ManifestDeployer implements Deployer {

        private final KubernetesTestInvoker invoker;

        public ManifestDeployer(KubernetesTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void deploy(KubernetesTestExtension extension) {
            invoker.copyManifests();
            invoker.deployManifests();
        }
    }

    public static class ScriptDeployer implements Deployer {

        private final KubernetesTestInvoker invoker;

        public ScriptDeployer(KubernetesTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void deploy(KubernetesTestExtension extension) {
            final RegularFile maybeScript = extension.getDeployment().getScript().getOrNull();
            if (maybeScript == null) {
                throw new GradleException("Please provide manifests or script into the deployment extension");
            }
            final File script = maybeScript.getAsFile();
            invoker.copyFolder(script.getParentFile());
            invoker.executeScriptWithEnvVar(script.getName(), extension.getNamespace().get());
        }
    }

    public static class ChartDeployer implements Deployer {
        private final KubernetesTestInvoker invoker;

        public ChartDeployer(KubernetesTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void deploy(KubernetesTestExtension extension) {
            invoker.installHelmChart(extension.getDeployment().getChart());
        }

    }


}
