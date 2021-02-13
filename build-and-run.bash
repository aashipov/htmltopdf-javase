#!/bin/bash

set -x

#To ease copy-paste
CONTAINER_NAME=htmltopdf-javase-dev
JDWP_PORT=8000
JMX_REMOTE_PORT=9998
HOST_IP="192.168.1.120"
PORTS_TO_PUBLISH="-p ${JMX_REMOTE_PORT}:${JMX_REMOTE_PORT} -p ${JDWP_PORT}:${JDWP_PORT} -p 8080:8080"
JAVA_TOOL_OPTIONS="JAVA_TOOL_OPTIONS=-XX:+PrintFlagsFinal -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${JDWP_PORT} -Djava.security.egd=file:/dev/./urandom -Djava.rmi.server.hostname=${HOST_IP} -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=${JMX_REMOTE_PORT} -Dcom.sun.management.jmxremote.rmi.port=${JMX_REMOTE_PORT} -Duser.timezone=UTC"

docker build --file=Dockerfile --tag=${CONTAINER_NAME} .
source ./down.bash
docker network create -d bridge ${CONTAINER_NAME}
docker run -d --name=${CONTAINER_NAME} --hostname=${CONTAINER_NAME} --network=${CONTAINER_NAME} -e "LC_ALL=en_US.UTF-8" -e "${JAVA_TOOL_OPTIONS}" ${PORTS_TO_PUBLISH} ${CONTAINER_NAME}
