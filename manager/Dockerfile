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

# ???? Always run update when changing package list, see https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
RUN apt update ; echo 'editthistoforcerun'

# install curl
RUN apt install -y curl

RUN apt install -y openjdk-16-jre

# fix so that we can find java
RUN java --version

RUN curl -O https://dlcdn.apache.org/kafka/3.0.0/kafka_2.13-3.0.0.tgz
RUN tar -xzf kafka_2.13-3.0.0.tgz
# /kafka_2.13-3.0.0

# install kubectl, instructions from kubernetes website
#RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
RUN curl -LO "https://dl.k8s.io/release/v1.22.3/bin/linux/amd64/kubectl"
RUN install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
RUN kubectl version --client

COPY build/libs/hom-impl-2.manager-1.0-SNAPSHOT.jar output.jar

# /usr/lib/jvm/jdk-17/bin/java -cp output.jar com.benblamey.hom.manager.ManagerMainTest
#ENTRYPOINT ["java","-jar","output.jar"]
ENTRYPOINT ["/bin/bash"]