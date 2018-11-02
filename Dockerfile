FROM govukpay/openjdk:alpine-3.8.1-jre-8.181.13

RUN apk update
RUN apk upgrade

RUN apk add --no-cache bash

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/
ADD docker-startup.sh /app/docker-startup.sh
ADD run-with-chamber.sh /app/run-with-chamber.sh

CMD bash ./docker-startup.sh
