FROM aashipov/htmltopdf:buildbed AS builder
ARG BUILD_DIR=/dummy/build
USER root
WORKDIR ${BUILD_DIR}
COPY --chown=dummy:dummy ./ ./
USER dummy
WORKDIR ${BUILD_DIR}
RUN mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

FROM aashipov/htmltopdf:base
ARG BUILD_DIR=/dummy/build
USER root
COPY --chown=dummy:dummy --from=builder ${BUILD_DIR}/target/htmltopdf*shaded.jar /dummy/app.jar
WORKDIR /dummy/
EXPOSE 8080
USER dummy
CMD java -jar app.jar
