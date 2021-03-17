FROM aashipov/htmltopdf:buildbed AS builder
ARG DUMMY_USER=dummy
ARG BUILD_DIR=/${DUMMY_USER}/build
ARG CHOWN_USER_AND_GROUP=${DUMMY_USER}:${DUMMY_USER}
USER root
WORKDIR ${BUILD_DIR}
COPY --chown=${CHOWN_USER_AND_GROUP} ./ ./
COPY --chown=${CHOWN_USER_AND_GROUP} settings.xml /dummy/.m2/
RUN chmod +x ${BUILD_DIR}/entrypoint.bash
USER dummy
WORKDIR ${BUILD_DIR}
RUN mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

FROM aashipov/htmltopdf:base
ARG DUMMY_USER=dummy
ARG BUILD_DIR=/${DUMMY_USER}/build
ARG CHOWN_USER_AND_GROUP=${DUMMY_USER}:${DUMMY_USER}
USER root
COPY --from=builder --chown=${CHOWN_USER_AND_GROUP} ${BUILD_DIR}/target/htmltopdf*shaded.jar /dummy/app.jar
COPY --from=builder --chown=${CHOWN_USER_AND_GROUP} ${BUILD_DIR}/entrypoint.bash /dummy/
WORKDIR /dummy/
EXPOSE 8080
USER dummy
ENTRYPOINT [ "/dummy/entrypoint.bash" ]
