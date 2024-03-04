FROM gradle:8.6.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Ensure your Gradle build script is compatible with Gradle 7.5
RUN gradle shadowJar


# Upgrade to JDK 17 for the runtime environment
FROM openjdk:21-jdk-slim
WORKDIR /src
COPY . /src

# Installation of utilities and setting permissions might remain unchanged
# Ensure all scripts and commands are compatible with JDK 17
RUN apt-get update && \
    apt-get install -y dos2unix && \
    dos2unix gradlew

WORKDIR /runA
COPY --from=build /home/gradle/src/build/libs/*.jar /run/server.jar

EXPOSE 8080

CMD ["java", "-jar", "/run/server.jar"]