# Gym CRM System (Spring Core)

This is a pure Spring Core implementation of a Gym CRM system. It operates completely without Spring Boot and uses an in-memory storage structure (Java Maps) configured via Spring bean post-processors and dependency injection.

## Prerequisites
* Java 17 (or higher) installed and configured in your system path.
* Maven 3.6+ installed.

## How to Run the Application

The application runs a built-in simulation inside the `App` main class to demonstrate the required CRUD operations and business logic.

To run the application from your terminal, you should:

**1. Open your terminal and navigate to the root directory of the project**
cd gym-crm-system

**2. Clean and compile using Maven commands:**
mvn clean compile  
**3. Run the application:**
mvn exec:java -Dexec.mainClass="com.gym.crm.App"  

**Expected Output**:  
The application should automatically:
- Initialize the Spring Application Context.
- Load the initial mock data from src/main/resources/init-data.json.
- Run the simulation, logging CREATE, SELECT, UPDATE, and DELETE operations directly in the console using SLF4J.
- Shut down after the simulation completes.
