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
RUN apt update ; echo 'editthistoforcerun6'

RUN apt install -y curl
#RUN apt install -y openjdk-16-jre
#RUN java --version

# Lines above are common to all Dockerfiles in the project.
######

RUN apt install -y python3.10-dev
RUN apt install -y python3-pip

COPY views/ /views/

CMD ["python3","-m","http.server","--directory","/views","8080"]


