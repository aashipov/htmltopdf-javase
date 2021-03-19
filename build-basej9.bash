#!/bin/bash

set -x

#To ease copy-paste
IMAGE="aashipov/htmltopdf-javase"
TAG="basej9"

docker build --file=Dockerfile.${TAG} --tag=${IMAGE}:${TAG} .
# Push VCS commit sha as docker hub tag to bypass nexus bug
docker push ${IMAGE}:${TAG}
