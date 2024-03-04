FROM gradle:7.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar

FROM openjdk:11-jdk-slim
WORKDIR /src
COPY . /src

RUN apt-get update
RUN apt-get install -y dos2unix
RUN dos2unix gradlew

WORKDIR /rung
COPY --from=build /home/gradle/src/build/libs/*.jar /run/server.jar

EXPOSE 8080

CMD java -jar /run/server.jar