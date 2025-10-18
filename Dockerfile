FROM amazoncorretto:21-alpine3.21

COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
