#!/bin/bash

set -e

apt-get update

apt-get install busybox -y

echo "hello world" > index.html

busybox httpd -p 9001