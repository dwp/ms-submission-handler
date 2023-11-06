FROM gcr.io/distroless/java17@sha256:2f01c2ff0c0db866ed73085cf1bb5437dd162b48526f89c1baa21dd77ebb5e6d
EXPOSE 9044

COPY ./target/ms-submission-handler*.jar /ms-submission-handler.jar
COPY ./src/main/properties/base-config.yml /opt/ms-submission-handler/config.yml
COPY ./src/main/resources/*.json /opt/ms-submission-handler/

WORKDIR /opt/ms-submission-handler

ENTRYPOINT [ "java", "-jar", "/ms-submission-handler.jar", "server", "/opt/ms-submission-handler/config.yml" ]
