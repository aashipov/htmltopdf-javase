#!/bin/bash

set - x

#To ease copy-paste
CONTAINER_NAME=htmltopdf-javase-dev

docker stop ${CONTAINER_NAME} ; docker rm ${CONTAINER_NAME}
