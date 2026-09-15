Feature: Trainee Profile Management
  As a registered trainee
  I want to be able to view, and manage my profile details
  So that my coaches have enough information about me and I can easily control my data

  Scenario: Retrieve a profile information
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid trainee profile exists for ID 1
    When the user sends a GET request to retrieve a trainee profile information for ID 1
    Then the response status is 200 (OK)
    And the trainee summary is returned
    And the ownership verification is triggered for 'TRAINEE' with ID 1
    And the trainee retrieval for ID 1 is processed by the trainee service

  Scenario: Fail to retrieve a profile information when unauthenticated
    Given an anonymous user
    When the user sends a GET request to retrieve a trainee profile information for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to retrieve a profile information for another user
    Given an authenticated user with a role 'TRAINEE' and ID 1
    But the system rejects authorization for 'TRAINEE' with ID 99
    When the user sends a GET request to retrieve a trainee profile information for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINEE' with ID 99

  Scenario: Fail to retrieve a profile information when user is not found
    Given an authenticated user with a role 'TRAINEE' and ID 1
    But the system cannot find trainee ID 1
    When the user sends a GET request to retrieve a trainee profile information for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainee not found"
    And the ownership verification is triggered for 'TRAINEE' with ID 1


  Scenario: Update trainee profile successfully
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee profile request
    When the user sends a PUT request to update the trainee profile for ID 1
    Then the response status is 200 (OK)
    And the trainee summary is returned
    And the ownership verification is triggered for 'TRAINEE' with ID 1
    And the update for ID 1 is processed by the trainee service

  Scenario Outline: Fail to update trainee profile due to validation errors
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And an update trainee profile request with invalid "<field_state>"
    When the user sends a PUT request to update the trainee profile for ID 1
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field          |
      | null full name        | fullName             |
      | blank first name      | fullName.firstName   |
      | blank last name       | fullName.lastName    |
      | underage birth date   | dateOfBirth          |
      | address too long      | address              |

  Scenario: Fail to update trainee profile when unauthenticated
    Given an anonymous user
    And a valid update trainee profile request
    When the user sends a PUT request to update the trainee profile for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to update the trainee profile for another user
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee profile request
    But the system rejects authorization for 'TRAINEE' with ID 99
    When the user sends a PUT request to update the trainee profile for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINEE' with ID 99

  Scenario: Fail to update trainee profile due to incorrect role
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid update trainee profile request
    When the user sends a PUT request to update the trainee profile for ID 1
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"

  Scenario: Fail to update the trainee profile when user is not found
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee profile request
    But the system cannot find trainee ID 1
    When the user sends a PUT request to update the trainee profile for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainee not found"
    And the ownership verification is triggered for 'TRAINEE' with ID 1


  Scenario: Delete trainee profile successfully
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    When the user sends a DELETE request to delete the trainee profile for username "John.Doe"
    Then the response status is 200 (OK)
    And the ownership verification is triggered for username "John.Doe"
    And the deletion for username "John.Doe" is processed by the trainee service

  Scenario: Fail to delete trainee profile when unauthenticated
    Given an anonymous user
    When the user sends a DELETE request to delete the trainee profile for username "John.Doe"
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to delete trainee profile due to incorrect role
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    When the user sends a DELETE request to delete the trainee profile for username "John.Doe"
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"


  Scenario: Change trainee activity status successfully
    Given an authenticated user with a role 'TRAINEE' and ID 1
    When the user sends a PATCH request to change trainee activity status for ID 1
    Then the response status is 200 (OK)
    And the ownership verification is triggered for 'TRAINEE' with ID 1
    And the activity status change for ID 1 is processed by the trainee service

  Scenario: Fail to change trainee activity status when unauthenticated
    Given an anonymous user
    When the user sends a PATCH request to change trainee activity status for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to change trainee activity status for another user
    Given an authenticated user with a role 'TRAINEE' and ID 1
    But the system rejects authorization for 'TRAINEE' with ID 99
    When the user sends a PATCH request to change trainee activity status for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINEE' with ID 99

  Scenario: Fail to change trainee activity status due to incorrect role
    Given an authenticated user with a role 'TRAINER' and ID 1
    When the user sends a PATCH request to change trainee activity status for ID 1
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"

  Scenario: Fail to change trainee activity status when user is not found
    Given an authenticated user with a role 'TRAINEE' and ID 1
    But the system cannot find trainee ID 1
    When the user sends a PATCH request to change trainee activity status for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainee not found"
    And the ownership verification is triggered for 'TRAINEE' with ID 1