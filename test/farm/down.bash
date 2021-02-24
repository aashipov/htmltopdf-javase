#!/bin/bash

set -x

NODE_NAMES=("htmltopdf-javase1" "htmltopdf-javase2" "htmltopdf-javase3")
NODE_NAMES_SPACE_SEPARATED=""
for node_name in "${NODE_NAMES[@]}"
do
    NODE_NAMES_SPACE_SEPARATED+=" ${node_name}"
done

HAPROXY=htmltopdf-javase-haproxy
NETWORK_NAME=htmltopdf-javase

docker stop ${HAPROXY} ${NODE_NAMES_SPACE_SEPARATED}
docker rm ${HAPROXY} ${NODE_NAMES_SPACE_SEPARATED}

docker network rm ${NETWORK_NAME}
