#!/bin/sh

helm package .

curl -u myUser:myPassword --data-binary "@mychart-0.1.0.tgz" http://host.docker.internal:8080/api/charts