FROM maven:3.9.11-amazoncorretto-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn -pl showcase -am clean package -DskipTests

FROM amazoncorretto:21-alpine3.21
WORKDIR /app
COPY --from=build /app/showcase/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
