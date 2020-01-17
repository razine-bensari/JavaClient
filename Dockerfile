#Using existing image
FROM maven:3.6.3-jdk-8

LABEL Razine Bensari <bensaria97@gmail.com>

WORKDIR /usr/app/JavaClient

COPY ./ ./

RUN mvn --version && \
    java -version && \
    cd ./sh/ && \
    chmod +x ./JarToBinary.sh && \
    ./JarToBinary.sh

ENV PATH="/usr/app/JavaClient/bin:${PATH}"