FROM maven:3.9.16-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/
COPY crm-service/pom.xml crm-service/
COPY trainer-workload-service/pom.xml trainer-workload-service/
RUN mvn -B dependency:go-offline -pl discovery-service -am

COPY discovery-service/src discovery-service/src
RUN mvn -B clean package -pl discovery-service -am -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/discovery-service/target/discovery-service-*.jar app.jar
USER spring:spring
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
