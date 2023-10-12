FROM gcr.io/distroless/java11@sha256:b7bfe2e8e38fc34180de94ec4b2e34a1fc4aa4021215edc87675710b95e663e2
EXPOSE 9044

COPY ./target/ms-submission-handler*.jar /ms-submission-handler.jar
COPY ./src/main/properties/base-config.yml /opt/ms-submission-handler/config.yml
COPY ./src/main/resources/*.json /opt/ms-submission-handler/

WORKDIR /opt/ms-submission-handler

ENTRYPOINT [ "java", "-jar", "/ms-submission-handler.jar", "server", "/opt/ms-submission-handler/config.yml" ]
