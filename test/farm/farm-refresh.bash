#!/bin/bash

# Run 3 htmltopdf containers and haproxy
set -x

HTML_TO_PDF_IMAGE="aashipov/htmltopdf-javase"
HAPROXY_IMAGE="haproxy"
THIS_DIR=$(pwd)
NODE_NAMES=("htmltopdf-javase1" "htmltopdf-javase2" "htmltopdf-javase3")
HAPROXY=htmltopdf-javase-haproxy
NETWORK_NAME=htmltopdf-javase
VOLUMES_HAPROXY="-v /${THIS_DIR}/haproxy/:/usr/local/etc/haproxy/:ro"
PORTS_TO_PUBLISH_HAPROXY="-p8080:8080 -p9999:9999"
MEMORY_LIMIT="256m"
JAVA_TOOL_OPTIONS="JAVA_TOOL_OPTIONS=-Xms=${MEMORY_LIMIT} -Xmx=${MEMORY_LIMIT} -Duser.timezone=UTC"

docker pull ${HTML_TO_PDF_IMAGE}
docker pull ${HAPROXY_IMAGE}
source ${THIS_DIR}/down.bash

docker network create -d bridge ${NETWORK_NAME}

for node_name in "${NODE_NAMES[@]}"
do
    docker run -d --name=${node_name} --hostname=${node_name} --net=${NETWORK_NAME} -e "LC_ALL=en_US.UTF-8" -e "${JAVA_TOOL_OPTIONS}" ${HTML_TO_PDF_IMAGE}
done

docker run -d --name=${HAPROXY} --hostname=${HAPROXY} --net=${NETWORK_NAME} ${PORTS_TO_PUBLISH_HAPROXY} ${VOLUMES_HAPROXY} ${HAPROXY_IMAGE}
