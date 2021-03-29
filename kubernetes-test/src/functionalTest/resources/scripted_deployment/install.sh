#!/usr/bin/env bash
set -e

kubectl --namespace=${NAMESPACE} apply -f deployments.yaml

kubectl --namespace=${NAMESPACE} apply -f services.yaml
