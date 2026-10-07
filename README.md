# Gym CRM System

This is a microservice implementation of a Gym CRM system. The services are built with Spring Boot 4 and REST API:  

`crm-service` manages the data layer with Hibernate/Spring Data JPA;  
`trainer-workload-service` manages the data layer with MongoDB storage.  
`discovery-service` manages the registration of services to dynamically resolve addresses via Eureka instead of hard-coded URLs.  

Services are deployed on embedded Tomcat containers managed automatically by Spring Boot, and leverage Eureka as a discovery service.

## Prerequisites
* Java 21 installed and configured in your system path.
* Maven 3.6+ installed.  
OR
* Docker Desktop installed (include Docker Engine and Docker Compose).

## Environment Configuration & Profiles

The application utilizes Spring Boot Profiles to manage different environments:
* `local`: For local development.
* `dev`: Shared remote development environment.
* `stg`: Pre-production staging environment.
* `prod`: Production configuration.

## Setup
- create .env in the root folder
- set such properties with your values to configure Docker (an example shown below):
````

# PostgreSQL Docker
POSTGRES_USER=postgres 
POSTGRES_PASSWORD=password1234
POSTGRES_DB=gym_db
DB_PORT=5435

# MongoDb Docker
MONGO_USER=admin
MONGO_PASSWORD=adminPassword
MONGO_DB=trainer_workloads
MONGO_PORT=27020

# JWT
JWT_SECRET_KEY=SdM6oy/wkfKckcHf2PX1oNTvmC4C7WteH4RLg6Lg6Nw=
JWT_EXPIRATION=7200000

# App services Docker
DISCOVERY_SERVICE_PORT=8761
CRM_SERVICE_PORT=8080
CRM_SERVICE_MANAGEMENT_PORT=8081
WORKLOAD_SERVICE_PORT=8082
WORKLOAD_SERVICE_MANAGEMENT_PORT=8083
KAFKA_PORT=9092
KAFKA_UI_PORT=8090
````

## Running Docker
The full stack - PostgreSQL, MongoDB, Kafka, Kafka UI, the Eureka `discovery-service`, `crm-service`, and `trainer-workload-service` - is orchestrated with Docker Compose on a shared network - `gym-network`.  
`crm-service` and `trainer-workload-service` reach PostgreSQL/MongoDB/Kafka via container hostnames (`postgres-db`, `mongodb`, `kafka`), and both register with `discovery-service`

1. Open your terminal and navigate to the root directory of the project with the `cd directory/` command 
2. Start the containers with the `docker compose up` command
3. Once the containers are up, the services are reachable at:
  * crm-service: `http://localhost:8080` (actuator/health on `8081`)
  * trainer-workload-service: `http://localhost:8082` (actuator/health on `8083`)
  * Eureka dashboard: `http://localhost:8761`
  * Kafka UI: `http://localhost:8090`

To view logs of the specific service: `docker compose logs -f crm-service`  
To shell into a running container: `docker exec -it gym_crm_service sh`  
To rebuild after code change: `docker compose up --build`

## How to Run the Application

To run the microservice application inside IntelliJ IDEA, you should:

- Click `Run` on the Spring Boot Services tab,  
OR
- Run each service individually in the following order:
  1. `DiscoveryServiceApplication`
  2. `TrainerWorkloadApplication`
  3. `CrmApplication`