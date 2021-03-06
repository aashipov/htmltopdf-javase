#!/bin/bash

set -x

#To ease copy-paste
IMAGE="aashipov/htmltopdf-javase"
TOP_COMMIT=$(git log --pretty=format:'%h' -n 1)
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
CONTAINER_NAME=htmltopdf-javase
JDWP_PORT=8000
JMX_REMOTE_PORT=9998
PORTS_TO_PUBLISH="-p ${JMX_REMOTE_PORT}:${JMX_REMOTE_PORT} -p ${JDWP_PORT}:${JDWP_PORT} -p 8080:8080"
JAVA_TOOL_OPTIONS="JAVA_TOOL_OPTIONS=-XX:+PrintFlagsFinal -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${JDWP_PORT} -Djava.security.egd=file:/dev/./urandom -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=${JMX_REMOTE_PORT} -Dcom.sun.management.jmxremote.rmi.port=${JMX_REMOTE_PORT} -Duser.timezone=UTC"

docker build --file=Dockerfile --tag=${IMAGE}:latest --tag=${IMAGE}:${TOP_COMMIT} --tag=${IMAGE}:${CURRENT_BRANCH} .
source ./down.bash
docker run -d --name=${CONTAINER_NAME} --hostname=${CONTAINER_NAME} -eLC_ALL=en_US.UTF-8 -e"${JAVA_TOOL_OPTIONS}" ${PORTS_TO_PUBLISH} ${IMAGE}:${TOP_COMMIT}
# Push VCS commit sha as docker hub tag to bypass nexus bug
docker push ${IMAGE}:latest
docker push ${IMAGE}:${TOP_COMMIT}
docker push ${IMAGE}:${CURRENT_BRANCH}
