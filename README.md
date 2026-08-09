# Gym CRM System

This is a Spring Web MVC and Hibernate-based REST API implementation of a Gym CRM system. The application operates **without Spring Boot**, utilizing a PostgreSQL database for persistent storage, and is deployed inside an embedded Tomcat servlet container managed by the Maven Cargo plugin.

## Prerequisites
* Java 21 installed and configured in your system path.
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

To run the application inside IntelliJ IDEA, you should:
- Open Run -> Edit Configurations...
  - Click the `+` button and select `Maven`.
  - Configure `Run (Command Line)`: clean package cargo:run
  - Paste the following into the Environment variables field:  
POSTGRES_USER=`user`;POSTGRES_PASSWORD=`password`;POSTGRES_DB=`database_name`;DB_PORT=`port`;DB_HOST=`host`

  - Click Apply
- Run the application by clicking `Run`

To run the application inside terminal (Windows PowerShell), you should:
- Open your terminal and navigate to the root directory of the project: `cd gym-crm-system`
- Paste in the command specifying the environment variables:  
`$env:POSTGRES_USER="postgres"; $env:POSTGRES_PASSWORD="password1234"; $env:POSTGRES_DB="gym_db"; $env:DB_PORT="5435"; $env:DB_HOST="localhost"; mvn clean package cargo:run`
