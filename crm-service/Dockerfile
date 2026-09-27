FROM maven:3.9.16-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY crm-service/pom.xml crm-service/
COPY trainer-workload-service/pom.xml trainer-workload-service/
COPY discovery-service/pom.xml discovery-service/
RUN mvn -B dependency:go-offline -pl crm-service -am

COPY crm-service/src crm-service/src
RUN mvn -B clean package -pl crm-service -am -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/crm-service/target/crm-service-*.jar app.jar
USER spring:spring
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
