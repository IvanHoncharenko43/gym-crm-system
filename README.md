# Gym CRM System

A microservice-based Gym CRM system built with Spring Boot 4. It manages trainees, trainers, and their training
sessions, tracks trainer workload, and can generate AI-driven, multi-week training programs for trainees.

## Architecture

The system is split into four Spring Boot services, each its own deployable/Maven module:

| Service | Responsibility | Storage | Ports (server / management) |
|---|---|---|---|
| `crm-service` | Core domain API: trainees, trainers, trainings, users. Owns JWT authentication/authorization, brute-force login protection, and the public-facing entry point for AI macrocycle generation. | PostgreSQL (Spring Data JPA) | 8080 / 8081 |
| `trainer-workload-service` | Maintains each trainer's monthly training-hour summary, kept in sync via Kafka. | MongoDB | 8082 / 8083 |
| `macrocycle-service` | Generates AI-driven, multi-week training macrocycles using Spring AI + Google Gemini, via an asynchronous planner/verifier agent loop. | MongoDB | 8084 / 8085 |
| `discovery-service` | Eureka server used by the other three services for service discovery. | — | 8761 |

**How they talk to each other:**
- `crm-service` calls `trainer-workload-service` and `macrocycle-service` synchronously over REST, resolved via Eureka and guarded by Resilience4j circuit breakers/retries.
- `crm-service` also publishes `TrainerWorkloadUpdateEvent`s to Kafka whenever a training is created or cancelled; `trainer-workload-service` consumes them asynchronously to keep its monthly summaries current.
- All three application services register with `discovery-service` on startup.

## Features

- JWT-based authentication and role-based authorization (`TRAINEE` / `TRAINER` / `ADMIN`)
- Brute-force login protection (temporary account lock after repeated failed logins) and JWT blacklisting on logout
- Trainee, trainer, and training management with per-resource ownership checks
- Kafka-driven trainer workload synchronization between `crm-service` and `trainer-workload-service`
- Resilience4j circuit breakers and retries around inter-service calls
- Eureka-based service discovery
- AI-generated long-form training macrocycles (Spring AI + Google Gemini): a planner/verifier agent loop drafts a multi-week plan, which the trainee reviews and explicitly approves before any real training sessions are created
- OpenAPI/Swagger documentation for each REST API

## Prerequisites

- Java 21+ installed and configured on your system path
- Maven 3.6+ installed
- Docker and Docker Compose (for PostgreSQL, MongoDB, and Kafka)
- A [Gemini API key](https://aistudio.google.com/apikey) (free tier available) if you want to exercise the macrocycle-generation feature

## Environment Configuration & Profiles

The application uses Spring Boot Profiles to manage different environments:
* `local`: For local development.
* `dev`: Shared remote development environment.
* `stg`: Pre-production staging environment.
* `prod`: Production configuration.

## Setup

Create a `.env` file in the repository root. It's used both by `docker compose` (for the container credentials) and,
if your run configuration loads it, by the Spring Boot services themselves.

**Docker container credentials:**
```
# PostgreSQL (crm-service)
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password1234
POSTGRES_DB=gym_db
DB_PORT=5435

# MongoDB (trainer-workload-service)
MONGO_USER=admin
MONGO_PASSWORD=adminPassword
MONGO_DB=trainer_workloads
MONGO_PORT=27020

# MongoDB (macrocycle-service)
MACROCYCLE_MONGO_USER=admin
MACROCYCLE_MONGO_PASSWORD=adminPassword
MACROCYCLE_MONGO_DB=macrocycles
MACROCYCLE_MONGO_PORT=27021
```

**`crm-service` (`local` profile) — variables without a built-in default:**
```
SPRING_PROFILES_ACTIVE=local
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/gym_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password1234
KAFKA_HOST=localhost:9092
```
> `KAFKA_HOST` has no fallback in `crm-service`'s `local` profile — it must be set, unlike `trainer-workload-service`, which defaults it to `localhost:9092`.

**`macrocycle-service` — required for AI generation to actually work:**
```
GEMINI_API_KEY=your-google-ai-studio-key
```
(`GEMINI_MODEL` defaults to `gemini-2.5-flash` if not set.)

**Shared, all with working local defaults — override only if you need to:**
```
JWT_SECRET_KEY=...
JWT_EXPIRATION=7200000
WORKLOAD_ID=trainer-workload-service
MACROCYCLE_ID=macrocycle-service
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

## Running Docker

The repository includes a `docker-compose.yaml` that starts PostgreSQL, both MongoDB instances, a single-node Kafka
broker (KRaft mode, no Zookeeper), and Kafka UI for inspecting topics/messages.

```bash
# from the repository root
docker compose up -d      # start all containers
docker compose down       # stop them
docker compose down -v    # stop and also wipe persisted volumes
```

## How to Run the Application

With the Docker containers running, start the services (Java processes) inside IntelliJ IDEA:

- Click `Run` on the Spring Boot Services compound run configuration,
  OR
- Run each service individually, in this order:
  1. `DiscoveryServiceApplication`
  2. `TrainerWorkloadApplication`
  3. `MacrocycleApplication`
  4. `CrmApplication`

## API Documentation

Once running, each service exposes Swagger UI (except `discovery-service`, which shows the Eureka dashboard):

| Service | URL |
|---|---|
| `crm-service` | http://localhost:8080/swagger-ui.html |
| `trainer-workload-service` | http://localhost:8082/swagger-ui.html |
| `macrocycle-service` | http://localhost:8084/swagger-ui.html |
| `discovery-service` (Eureka dashboard) | http://localhost:8761 |
| Kafka UI | http://localhost:8090 |

## Testing

Run all unit tests from the repository root:
```bash
mvn test
```

Run a specific test class:
```bash
mvn test -Dtest=TraineeRepositoryIT
```

Integration tests use `Testcontainers` (PostgreSQL for `crm-service`, MongoDB for
`trainer-workload-service` and `macrocycle-service`) and spin up their own ephemeral containers independent of the
`docker-compose.yaml` dev environment — **Docker must be running** for these to pass. `crm-service` additionally has
Cucumber BDD feature tests covering registration, authentication, trainee/trainer profile management, trainer
assignment, training reporting, and password changes.
