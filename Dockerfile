FROM gcr.io/distroless/java17@sha256:56958eb07314c7a4745ae9f40eb86ff6e04bf60bdcf5bd3218ccc28daeaa5b00
EXPOSE 9044

COPY ./target/ms-submission-handler*.jar /ms-submission-handler.jar
COPY ./src/main/properties/base-config.yml /opt/ms-submission-handler/config.yml
COPY ./src/main/resources/*.json /opt/ms-submission-handler/

WORKDIR /opt/ms-submission-handler

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

ENTRYPOINT [ "java", "-jar", "/ms-submission-handler.jar", "server", "/opt/ms-submission-handler/config.yml" ]
