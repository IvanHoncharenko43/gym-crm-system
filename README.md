# Gym CRM System

This is a pure Spring Core implementation of a Gym CRM system. It operates completely without Spring Boot and uses an in-memory storage structure (Java Maps) configured via Spring bean post-processors and dependency injection.

## Prerequisites
* Java 17 (or higher) installed and configured in your system path.
* Maven 3.6+ installed.

## Setup
**Configure database credentials:**  
- create .env in the root folder
- set such properties with your values:
  -  POSTGRES_USER=`user`
  -  POSTGRES_PASSWORD=`password`
  -  POSTGRES_DB=`database_name`
  -  DB_PORT=`port`
  -  DB_HOST=`host`

## Running Docker
- Open your terminal and navigate to the root directory of the project with the `cd directory/` command
- Start the database container with the `docker compose up` command
## How to Run the Application

The application runs a built-in simulation inside the `App` main class to demonstrate the required CRUD operations and business logic.

To run the application from your terminal, you should:

- Open your terminal and navigate to the root directory of the project: `cd gym-crm-system`

- Clean and compile using Maven commands: `mvn clean compile`  
- Run the application: `mvn exec:java -Dexec.mainClass="org.example.App"`
