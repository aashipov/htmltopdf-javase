#!/bin/bash

set - x

#To ease copy-paste
CONTAINER_NAME=htmltopdf-javase

docker stop ${CONTAINER_NAME}
docker rm ${CONTAINER_NAME}
docker network rm ${CONTAINER_NAME}
