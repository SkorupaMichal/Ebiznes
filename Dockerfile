FROM ubuntu:18.04

RUN apt-get update -y && \
    apt-get install -y software-properties-common gnupg2 apt-transport-https apt-utils

RUN apt-get install -y openjdk-8-jdk openjdk-8-jre
RUN apt-get update && \
    apt-get install -y \
    curl \
    vim \
    git \
    unzip

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update -y && \
    apt-get install -y sbt

RUN curl -sLO https://downloads.lightbend.com/scala/2.12.8/scala-2.12.8.deb && \
    dpkg -i scala-2.12.8.deb

RUN curl -sL https://deb.nodesource.com/setup_13.x | bash - && \
    apt-get install -y nodejs
RUN npm install -g npm@6.8
RUN mkdir -p /home/michal_skorupa/projekt
WORKDIR /home/michal_skorupa/projekt
EXPOSE 8000
EXPOSE 9000
EXPOSE 5000
EXPOSE 8888
VOLUME [ "/home/michal_skorupa/projekt" ]
