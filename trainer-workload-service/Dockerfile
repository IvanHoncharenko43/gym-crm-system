FROM maven:3.9.16-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY crm-service/pom.xml crm-service/
COPY trainer-workload-service/pom.xml trainer-workload-service/
COPY discovery-service/pom.xml discovery-service/
RUN mvn -B dependency:go-offline -pl trainer-workload-service -am

COPY trainer-workload-service/src trainer-workload-service/src
RUN mvn -B clean package -pl trainer-workload-service -am -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/trainer-workload-service/target/trainer-workload-service-*.jar app.jar
USER spring:spring
EXPOSE 8082 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
