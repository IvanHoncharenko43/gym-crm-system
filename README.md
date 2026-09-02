# Gym CRM System

This is a microservice implementation of a Gym CRM system. The services are built with Spring Boot 4 and REST API:  
`crm-service` manages the data layer with Hibernate/Spring Data JPA;  
`trainer-workload-service` manages the data layer with in-memory storage.  
Services are deployed on embedded Tomcat containers managed automatically by Spring Boot, and leverage Eureka as a discovery service.

## Prerequisites
* Java 21 installed and configured in your system path.
* Maven 3.6+ installed.

## Environment Configuration & Profiles

The application utilizes Spring Boot Profiles to manage different environments:
* `local`: For local development.
* `dev`: Shared remote development environment.
* `stg`: Pre-production staging environment.
* `prod`: Production configuration.

## Setup
**Configure database credentials:**  
- create .env in the root folder
- set such properties with your values to configure Docker:
  -  POSTGRES_USER=`user`
  -  POSTGRES_PASSWORD=`password`
  -  POSTGRES_DB=`database_name`
  -  DB_PORT=`port`
- set such properties with your values to inject system environment variables:
  -  SPRING_PROFILES_ACTIVE=`profile`
  -  SPRING_DATASOURCE_URL=`url`
  -  SPRING_DATASOURCE_USERNAME=`username`
  -  SPRING_DATASOURCE_PASSWORD=`port`

## Running Docker
- Open your terminal and navigate to the root directory of the project with the `cd directory/` command
- Start the database container with the `docker compose up` command
## How to Run the Application

To run the microservice application inside IntelliJ IDEA, you should:

- Click `Run` on the Spring Boot Services tab,  
OR
- Run each service individually in the following order:
  1. `DiscoveryServiceApplication`
  2. `TrainerWorkloadApplication`
  3. `CrmApplication`