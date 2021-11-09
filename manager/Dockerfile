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

# install Oracle JDK 17
RUN curl -LO https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
RUN apt install -y ./jdk-17_linux-x64_bin.deb

# install kubectl, instructions from kubernetes website
#RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
RUN curl -LO "https://dl.k8s.io/release/v1.22.3/bin/linux/amd64/kubectl"
RUN install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
RUN kubectl version --client


#COPY build/libs/hom-impl-2.manager-1.0-SNAPSHOT.jar output.jar

#ENTRYPOINT ["java","-jar","output.jar"]
ENTRYPOINT ["/bin/bash"]

