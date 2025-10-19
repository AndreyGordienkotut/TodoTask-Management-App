#2 версия
FROM gradle:8.14.3-jdk17 as build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN chmod +x ./gradlew

RUN ./gradlew :taskService:bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /home/gradle/project/taskService/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
#1 версия
#FROM gradle:8.4-jdk21 AS build
#WORKDIR /app
#COPY . .
#RUN ./gradlew clean build -x test --no-daemon
#
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY --from=build /app/build/libs/*.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]