FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/validafraude-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]