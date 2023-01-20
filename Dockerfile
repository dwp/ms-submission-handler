FROM gcr.io/distroless/java11@sha256:e0835ed9898c4e486761b1b1018bf8a92bdd04a8390038cf97e3dc7dbfcf25d8
EXPOSE 9044

COPY ./target/ms-submission-handler*.jar /ms-submission-handler.jar
COPY ./src/main/properties/base-config.yml /opt/ms-submission-handler/config.yml
COPY ./src/main/resources/*.json /opt/ms-submission-handler/

WORKDIR /opt/ms-submission-handler

ENTRYPOINT [ "java", "-jar", "/ms-submission-handler.jar", "server", "/opt/ms-submission-handler/config.yml" ]
