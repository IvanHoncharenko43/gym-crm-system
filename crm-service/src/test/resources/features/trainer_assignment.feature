Feature: Trainer Assignment
  As a registered trainee
  I want to view available trainers and update my assigned trainers list
  So that I can customize my fitness training program

  Scenario: Retrieve not assigned trainers successfully
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    When the user sends a GET request to retrieve not assigned trainers for username "John.Doe"
    Then the response status is 200 (OK)
    And the trainers list is returned
    And the ownership verification is triggered for username "John.Doe"
    And the unassigned trainers retrieval for "John.Doe" is processed by the trainer service

  Scenario: Fail to retrieve not assigned trainers due to validation errors
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    When the user sends a GET request to retrieve not assigned trainers for username "  "
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing constraint violations

  Scenario: Fail to retrieve not assigned trainers when unauthenticated
    Given an anonymous user
    When the user sends a GET request to retrieve not assigned trainers for username "John.Doe"
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to retrieve not assigned trainers for another user
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    But the system rejects authorization for username "Jane.Doe"
    When the user sends a GET request to retrieve not assigned trainers for username "Jane.Doe"
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for username "Jane.Doe"

  Scenario: Fail to retrieve not assigned trainers due to incorrect role
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    When the user sends a GET request to retrieve not assigned trainers for username "John.Doe"
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"


  Scenario: Update a trainee's trainers list successfully
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee trainers request
    When the user sends a PUT request to update the trainers list for ID 1
    Then the response status is 200 (OK)
    And the updated trainers list is returned
    And the ownership verification is triggered for 'TRAINEE' with ID 1
    And the trainers list update for ID 1 is processed by the trainee service

  Scenario Outline: Fail to update a trainee's trainers list due to validation errors
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And an update trainee trainers request with invalid "<field_state>"
    When the user sends a PUT request to update the trainers list for ID 1
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state            | error_field         |
      | empty list             | trainerUsernames    |
      | blank username in list | trainerUsernames[1] |

  Scenario: Fail to update a trainee's trainers list when unauthenticated
    Given an anonymous user
    And a valid update trainee trainers request
    When the user sends a PUT request to update the trainers list for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to update a trainee's trainers list for another user
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee trainers request
    But the system rejects authorization for 'TRAINEE' with ID 99
    When the user sends a PUT request to update the trainers list for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINEE' with ID 99

  Scenario: Fail to update a trainee's trainers list due to incorrect role
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid update trainee trainers request
    When the user sends a PUT request to update the trainers list for ID 1
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"

  Scenario: Fail to update a trainee's trainers list when user is not found
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainee trainers request
    But the system cannot find trainee ID 1 when updating trainers
    When the user sends a PUT request to update the trainers list for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainee not found"
    And the ownership verification is triggered for 'TRAINEE' with ID 1