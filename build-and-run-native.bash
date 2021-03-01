#!/bin/bash

set -x

#To ease copy-paste
CONTAINER_NAME=htmltopdf-javase
IMAGE="aashipov/${CONTAINER_NAME}:native"

docker build --file=Dockerfile-native --tag=${IMAGE} .
source ./down.bash
docker run -d --name=${CONTAINER_NAME} --hostname=${CONTAINER_NAME} -p 8080:8080 ${IMAGE}
