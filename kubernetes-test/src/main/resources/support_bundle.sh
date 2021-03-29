#!/usr/bin/env bash

set +e

K8S_NAMESPACE=
SUPPORT_BUNDLE_FILENAME=support-bundle.tar
K8S_CMD=kubectl
K8S_NS_OPT=--namespace=${K8S_NAMESPACE}

echo "Generating support bundle..."

all_pods_info() {
    local pod=$1
    local out_dir=$2
    $K8S_CMD $K8S_NS_OPT describe pod $pod > "$out_dir/describe_pod"
    $K8S_CMD $K8S_NS_OPT logs $pod > "$out_dir/stdout.log"
    for initContainer in $($K8S_CMD $K8S_NS_OPT get pod $pod -o jsonpath='{..initContainers[*].name}'); do
        $K8S_CMD $K8S_NS_OPT logs $pod -c $initContainer > "$out_dir/$initContainer-init-stdout.log"
    done
}

cluster_info() {
    mkdir -p bundle_temp/cluster
    $K8S_CMD version > ./bundle_temp/cluster/version
    $K8S_CMD get persistentvolumes -o json > ./bundle_temp/cluster/get-persistentvolumes.json
}

persistentvolumes_info() {
    mkdir -p bundle_temp/persistentvolumes

    for persistentvolume in $($K8S_CMD -o jsonpath='{.items[*].metadata.name}' get persistentvolumes); do
        $K8S_CMD describe persistentvolumes $persistentvolume > "./bundle_temp/persistentvolumes/describe-persistentvolumes-$persistentvolume"
    done
}

nodes_info() {
    mkdir -p bundle_temp/node
    nodes=$($K8S_CMD $K8S_NS_OPT  --selector=app=gradle-enterprise -o jsonpath='{.items[*].spec.nodeName}' get pods)
    uniqueNodes=$(echo -e "${nodes// /\\n}" | sort -u)
    for nodeName in $uniqueNodes; do
        $K8S_CMD describe node "$nodeName" > "./bundle_temp/node/describe-node-$nodeName"
    done
}

namespace_info() {
    mkdir -p bundle_temp/namespace
    $K8S_CMD $K8S_NS_OPT get pods -o json > ./bundle_temp/namespace/get-pods.json
    $K8S_CMD $K8S_NS_OPT get configmaps -o json > ./bundle_temp/namespace/get-configmaps.json
    $K8S_CMD $K8S_NS_OPT get services -o json > ./bundle_temp/namespace/get-services.json
    $K8S_CMD $K8S_NS_OPT get persistentvolumeclaims -o json > ./bundle_temp/namespace/get-persistentvolumeclaims.json
}

pods_info() {
    mkdir -p bundle_temp/pods
    for pod in $($K8S_CMD $K8S_NS_OPT get pods); do
        component=$($K8S_CMD $K8S_NS_OPT get pod $pod -o jsonpath='{.metadata.labels.component}')
        out_dir=./bundle_temp/pods/$component
        mkdir -p "$out_dir/commands"
        all_pods_info $pod $out_dir
    done
}

main() {
    rm -rf bundle_temp
    mkdir -p bundle_temp/

    exec 2> bundle_temp/support-bundle-error.log

    cluster_info
    persistentvolumes_info
    nodes_info
    namespace_info
    pods_info

    cd bundle_temp
    tar -cf ../${SUPPORT_BUNDLE_FILENAME} .
    cd ..
    rm -rf bundle_temp
}

main

echo "Support bundle file ${SUPPORT_BUNDLE_FILENAME} created"