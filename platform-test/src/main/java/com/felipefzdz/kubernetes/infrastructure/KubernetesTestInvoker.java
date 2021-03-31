package com.felipefzdz.kubernetes.infrastructure;

import com.felipefzdz.base.infrastructure.HttpProbe;
import com.felipefzdz.base.infrastructure.Shell;
import com.felipefzdz.kubernetes.extension.Chart;
import com.felipefzdz.kubernetes.extension.Deployment;
import com.felipefzdz.kubernetes.tasks.CleanupKubernetesTask;
import com.felipefzdz.kubernetes.tasks.DeployKubernetesTask;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

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

    private static final Logger logger = Logging.getLogger(KubernetesTestInvoker.class);

    public static void setup(DeployKubernetesTask task) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Shell shell = new Shell(logger, task.getProjectDir());
            setupK3d(task, shell);
            logger.info("K3d setup elapsed time: " + stopwatch);
            maybeCreateNamespace(task, shell);
            deployDeployment(task, shell);
            exposeService(task, shell);
            executeProbe(task, shell);
            logger.info("Deployment available elapsed time: " + stopwatch);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the Kubernetes setup", e);
        }

    }

    public static void cleanup(CleanupKubernetesTask task) {
        try {
            Shell shell = new Shell(logger, task.getProjectDir());
            cleanupK3d(task, shell);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the Kubernetes cleanup", e);
        }
    }


    private static void setupK3d(DeployKubernetesTask task, Shell shell) {
        final List<String> command = join(getK3dClusterCommand(task.getK3dVersion()), "create", "-p", task.getProbe().getPort().get() + ":80@loadbalancer", "test-k3d");
        shell.run(command);
    }

    private static void cleanupK3d(CleanupKubernetesTask task, Shell shell) {
        shell.run(join(getK3dClusterCommand(task.getK3dVersion()), "delete", "test-k3d"));
    }

    private static void maybeCreateNamespace(DeployKubernetesTask task, Shell shell) {
        final String namespace = task.getNamespace().get();
        if (!"default".equals(namespace)) {
            shell.run(join(getKubectlCommand(), "create", "namespace", namespace));
        }
    }

    private static void copyManifests(DeployKubernetesTask task, Shell shell) {
        task.getDeployment().getManifests().getFiles().forEach(file -> copyFile(file, shell));
    }

    private static void copyFile(File file, Shell shell) {
        shell.run(asList("docker", "cp", file.getAbsolutePath(), "k3d-test-k3d-server-0:/" + file.getName()));
    }

    private static void copyFolder(File file, Shell shell) {
        shell.run(asList("docker", "cp", file.getAbsolutePath() + "/.", "k3d-test-k3d-server-0:/"));
    }

    private static void downloadSupportBundle(Shell shell) {
        shell.run(asList("docker", "cp", "k3d-test-k3d-server-0:/support-bundle.tar", "support-bundle.tar"));
    }

    private static void deployManifests(DeployKubernetesTask task, Shell shell) {
        task.getDeployment().getManifests().getFiles().forEach(manifest -> deployManifest(task, manifest, shell));
    }

    private static void exposeService(DeployKubernetesTask task, Shell shell) {
        try {
            String ingressContent = IOUtils.toString(KubernetesTestInvoker.class.getClassLoader().getResourceAsStream("ingress.yaml"), UTF_8);
            File ingressManifest = File.createTempFile("temp", "ingress.yaml");
            String ingressManifestInterpolated = ingressContent
                    .replace("name:", "name: " + task.getDeployment().getEdge().getName().get())
                    .replace("number:", "number: " + task.getDeployment().getEdge().getPort().get());
            FileUtils.writeStringToFile(ingressManifest, ingressManifestInterpolated, UTF_8);
            copyFile(ingressManifest, shell);
            deployManifest(task, ingressManifest, shell);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deployManifest(DeployKubernetesTask task, File manifest, Shell shell) {
        shell.run(join(getKubectlWithNamespaceCommand(task.getNamespace().get()), "apply", "-f", manifest.getName()));
    }

    private static void executeScript(String script, Shell shell) {
        shell.run(asList("docker", "exec", "k3d-test-k3d-server-0", "sh", script));
    }

    private static void executeScriptWithEnvVar(String script, String envVar, Shell shell) {
        shell.run(asList("docker", "exec", "-e", "NAMESPACE=" + envVar, "k3d-test-k3d-server-0", "sh", script));
    }

    private static void executeProbe(DeployKubernetesTask task, Shell shell) {
        final HttpProbe.HttpProbeStatus result = HttpProbe.run(task.getProbe());
        if (result.failure) {
            generateSupportBundle(task, shell);
            throw new GradleException("Error while probing the application", result.maybeException.get());
        }
    }

    private static void generateSupportBundle(DeployKubernetesTask task , Shell shell) {
        try {
            String supportBundleContent = IOUtils.toString(KubernetesTestInvoker.class.getClassLoader().getResourceAsStream("support_bundle.sh"), UTF_8);
            File supportBundleScript = File.createTempFile("temp", "support_bundle.sh");
            String supportBundleContentInterpolated = supportBundleContent.replace("K8S_NAMESPACE=", "K8S_NAMESPACE=" + task.getNamespace());
            FileUtils.writeStringToFile(supportBundleScript, supportBundleContentInterpolated, UTF_8, true);
            copyFile(supportBundleScript, shell);
            executeScript(supportBundleScript.getName(), shell);
            downloadSupportBundle(shell);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getK3dClusterCommand(String k3dVersion) {
        return asList("docker", "run", "--rm", "-v", "/var/run/docker.sock:/var/run/docker.sock", "rancher/k3d:" + k3dVersion, "cluster");
    }

    private static List<String> getKubectlCommand() {
        return asList("docker", "exec", "k3d-test-k3d-server-0", "kubectl");
    }

    private static List<String> getKubectlWithNamespaceCommand(String namespace) {
        return join(getKubectlCommand(), "--namespace", namespace);
    }

    private static List<String> join(List<String> firstPartCommand, String... secondPartCommand) {
        return Stream.concat(firstPartCommand.stream(), Stream.of(secondPartCommand))
                .collect(Collectors.toList());
    }

    private static void deployDeployment(DeployKubernetesTask task, Shell shell) {
        Deployer.of(task.getDeployment()).deploy(task, shell);
    }

    private static void installHelmChart(Chart chart, Shell shell) {
        try {
            File installChartScript = createInstallChartScript(chart, shell);
            shell.run(asList("docker", "run", "-v", createKubeconfig(shell).getAbsolutePath() + ":/root/.kube/config",
                    "-v", installChartScript.getAbsolutePath() + ":/config/" + installChartScript.getName(), "--rm",
                    "dtzar/helm-kubectl", "/bin/sh", "-c", "\"/config/" + installChartScript.getName() + "\""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File createInstallChartScript(Chart chart, Shell shell) throws IOException {
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

    private static Optional<String> addToRepoCommand(Chart chart) {
        if (chart.getUser().getOrNull() == null || chart.getPassword().getOrNull() == null) {
            return Optional.empty();
        }
        return Optional.of("http://host.docker.internal:8080 --username " + chart.getUser().get() + " --password " + chart.getPassword().get());
    }

    private static String interpolateContent(Map<String, Optional<String>> interpolators, String content) {
        for (Map.Entry<String, Optional<String>> interpolator : interpolators.entrySet()) {
            if (interpolator.getValue().isPresent()) {
                content = content.replace(interpolator.getKey(), interpolator.getValue().get());
            }
        }
        return content;
    }

    private static File createKubeconfig(Shell shell) throws IOException {
        String kubeConfigContent = shell.run(asList("docker", "exec", "k3d-test-k3d-server-0", "kubectl", "config", "view", "--raw"));
        String k3sPort = shell.run(asList("docker", "port", "k3d-test-k3d-serverlb", "6443")).replace("0.0.0.0", "host.docker.internal");
        String kubeConfigContentInterpolated = kubeConfigContent.replace("127.0.0.1:6443", k3sPort);
        File kubeConfigScript = File.createTempFile("temp", "config");
        FileUtils.writeStringToFile(kubeConfigScript, kubeConfigContentInterpolated, UTF_8, true);
        return kubeConfigScript;
    }

    public interface Deployer {
        void deploy(DeployKubernetesTask task, Shell shell);

        static Deployer of(Deployment deployment) {
            if (deployment.getManifests() != null) {
                return new ManifestDeployer();
            }
            if (deployment.getScript().getOrNull() != null) {
                return new ScriptDeployer();
            }
            return new ChartDeployer();
        }
    }

    public static class ManifestDeployer implements Deployer {

        @Override
        public void deploy(DeployKubernetesTask task, Shell shell) {
            copyManifests(task, shell);
            deployManifests(task, shell);
        }
    }

    public static class ScriptDeployer implements Deployer {
        @Override
        public void deploy(DeployKubernetesTask task, Shell shell) {
            final RegularFile maybeScript = task.getDeployment().getScript().getOrNull();
            if (maybeScript == null) {
                throw new GradleException("Please provide manifests or script into the deployment extension");
            }
            final File script = maybeScript.getAsFile();
            copyFolder(script.getParentFile(), shell);
            executeScriptWithEnvVar(script.getName(), task.getNamespace().get(), shell);
        }
    }

    public static class ChartDeployer implements Deployer {
        @Override
        public void deploy(DeployKubernetesTask task, Shell shell) {
            installHelmChart(task.getDeployment().getChart(), shell);
        }

    }


}
