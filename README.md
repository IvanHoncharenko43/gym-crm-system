# Gym CRM System

This is a Spring Boot 4 and Hibernate/Spring Data JPA-based REST API implementation of a Gym CRM system. The application deploys on an embedded Tomcat container managed automatically by Spring Boot

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

To run the application inside IntelliJ IDEA, you should:

POSTGRES_USER=`user`;POSTGRES_PASSWORD=`password`;POSTGRES_DB=`database_name`;DB_PORT=`port`;DB_HOST=`host`

- Click `Run` to run the GymCrmApplication