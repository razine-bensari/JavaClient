#Using existing image
FROM maven:3.6.3-jdk-8

LABEL Razine Bensari <bensaria97@gmail.com>

WORKDIR /usr/app/JavaClient

COPY ./ ./

CMD ["mvn", "--version"]
CMD ["java", "-version"]
