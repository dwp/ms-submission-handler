FROM gcr.io/distroless/java17@sha256:8bb82ccf73085b71159ce05d2cc6030cbaa927b403c04774f0b22f37ab4fd78a
EXPOSE 9044

COPY ./target/ms-submission-handler*.jar /ms-submission-handler.jar
COPY ./src/main/properties/base-config.yml /opt/ms-submission-handler/config.yml
COPY ./src/main/resources/*.json /opt/ms-submission-handler/

WORKDIR /opt/ms-submission-handler

ENTRYPOINT [ "java", "-jar", "/ms-submission-handler.jar", "server", "/opt/ms-submission-handler/config.yml" ]
