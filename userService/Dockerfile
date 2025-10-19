
FROM gradle:8.14.3-jdk17 as build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN chmod +x ./gradlew

RUN ./gradlew :userService:bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /home/gradle/project/userService/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]