FROM java:8-jdk-alpine

COPY ./target/bot-svc-1.0.0.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch bot-svc-1.0.0.jar'

ENTRYPOINT ["java", "-jar", "bot-svc-1.0.0.jar"]