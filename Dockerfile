FROM aashipov/htmltopdf:buildbed AS builder
USER root
RUN mkdir /dummy/build/ && chown -R dummy:dummy /dummy/build/
WORKDIR /dummy/build/
COPY --chown=dummy:dummy ./ ./
USER dummy
WORKDIR /dummy/build/
RUN mvn clean install

FROM aashipov/htmltopdf:base
USER root
COPY --chown=dummy:dummy --from=builder /dummy/build/target/htmltopdf*shaded.jar /dummy/app.jar
WORKDIR /dummy/
EXPOSE 8080
USER dummy
CMD java -jar app.jar
