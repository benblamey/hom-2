FROM ubuntu:hirsute
# Ubuntu 21.04

# with ubuntu:impish, get an error on apt update
# Problem executing scripts APT::Update::Post-Invoke 'rm -f /var/cache/apt/archives/*.deb /var/cache/apt/archives/partial/*.deb /var/cache/apt/*.bin || true'

# set locale¨
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

# set timezone, see: https://stackoverflow.com/a/48430880/795671
ENV TZ=Europe/Stockholm
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Always run update when changing package list, see https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
RUN apt update ; echo 'editthistoforcerun'

# install curl
RUN apt install -y curl
RUN apt install -y openjdk-16-jre

RUN java --version

# Lines above are common to all Dockerfiles in the project.
######



RUN curl -O https://dlcdn.apache.org/kafka/3.0.0/kafka_2.13-3.0.0.tgz

RUN tar -xzf kafka_2.13-3.0.0.tgz

# this would be just a comment to say which port it listens on!
#EXPOSE 29092

WORKDIR /kafka_2.13-3.0.0

COPY log4j.properties config/log4j.properties
#CMD ["bash"]

# Sleep 20 seconds to allow ZooKeeper to declare any previous Kafka broker as dead.
# "Introduce a 20 second delay between zookeeper and kafka. The zookeeper has a session expiry time of 18000ms. It needs this time to declare the old session dead. If the Kafka broker is brought up before this happens, the broker shuts down with "Error while creating ephemeral at /broker/ids/1, node already exists". You can thus create an entrypoint
# https://github.com/wurstmeister/kafka-docker/issues/389

# Separate issue. Need to clear meta.properties file, line: cluster.id=xxxx, see:
# https://stackoverflow.com/questions/59592518/kafka-broker-doesnt-find-cluster-id-and-creates-new-one-after-docker-restart
# Hence the sed
# || true so we don't abort on failure (on first time)
# (TODO:) With this fix we are running a single zookeeper/kafka instance.

# ALICE for connections from within other Kafka pods.
# BOB for localhost connections from the host machine (outside kafka)

# This starts both Zookeeper and Kafka. In the YAML we override this and start separate containers for each.
# That way if a process dies, the pod is restarted.
CMD ["bash","-ec","{ sed '/^cluster/d' data/meta.properties || true ; ./bin/zookeeper-server-start.sh config/zookeeper.properties; } & { sleep 20 ; ./bin/kafka-server-start.sh config/server.properties --override listeners=ALICE://0.0.0.0:9092,BOB://0.0.0.0:19092 --override advertised.listeners=ALICE://kafka-service:9092,BOB://localhost:19092 --override listener.security.protocol.map=ALICE:PLAINTEXT,BOB:PLAINTEXT --override inter.broker.listener.name=ALICE --override auto.create.topics.enable=true; }"]