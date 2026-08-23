FROM eclipse-temurin:21

ENV TZ=Asia/Seoul

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Dspring.config.additional-location=file:/app/config/application-secret.yml", "-Dspring.profiles.active=${USE_PROFILE}", "-jar", "/app.jar"]
