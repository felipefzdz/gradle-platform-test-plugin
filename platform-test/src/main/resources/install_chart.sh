#!/bin/sh

helm repo add {repo} http://host.docker.internal:8080

helm repo update

helm --debug install {release} {repo}/{name} --version {version}
