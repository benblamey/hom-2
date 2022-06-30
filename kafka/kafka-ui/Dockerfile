FROM alpine:3.15.0

CMD ["/bin/sh"]

RUN apk add --no-cache openjdk13-jre libc6-compat gcompat \
&& addgroup -S kafkaui && adduser -S kafkaui -G kafkaui

USER kafkaui

COPY "/target/kafka-ui-api-v0.4.0.jar" "/kafka-ui-api.jar"

ENV JAVA_OPTS=

EXPOSE 8088

CMD java $JAVA_OPTS -jar kafka-ui-api.jar
