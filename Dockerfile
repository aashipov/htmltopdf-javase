FROM aashipov/htmltopdf:buildbed AS builder
ARG BUILD_DIR=/dummy/build
USER root
WORKDIR ${BUILD_DIR}
COPY --chown=dummy:dummy ./ ./
COPY --chown=${CHOWN_USER_AND_GROUP} settings.xml /dummy/.m2/
RUN chmod +x ${BUILD_DIR}/entrypoint.bash && chown -R dummy:dummy /dummy/
USER dummy
WORKDIR ${BUILD_DIR}
RUN mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

FROM aashipov/htmltopdf:base
ARG BUILD_DIR=/dummy/build
USER root
COPY --chown=dummy:dummy --from=builder ${BUILD_DIR}/target/htmltopdf*shaded.jar /dummy/app.jar
COPY --from=builder --chown=dummy:dummy ${BUILD_DIR}/entrypoint.bash /dummy/
WORKDIR /dummy/
EXPOSE 8080
USER dummy
ENTRYPOINT [ "/dummy/entrypoint.bash" ]
