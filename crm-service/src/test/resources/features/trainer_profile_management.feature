Feature: Trainer Profile Management
  As a registered trainer
  I want to be able to view, update, and manage my profile
  So that my information remains accurate and my account can be controlled

  Scenario: Retrieve a profile information
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid trainer profile exists for ID 1
    When the user sends a GET request to retrieve a trainer profile information for ID 1
    Then the response status is 200 (OK)
    And the trainer summary is returned
    And the ownership verification is triggered for 'TRAINER' with ID 1
    And the trainer retrieval for ID 1 is processed by the trainer service

  Scenario: Fail to retrieve a profile information when unauthenticated
    Given an anonymous user
    When the user sends a GET request to retrieve a trainer profile information for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to retrieve a profile information for another user
    Given an authenticated user with a role 'TRAINER' and ID 1
    But the system rejects authorization for 'TRAINER' with ID 99
    When the user sends a GET request to retrieve a trainer profile information for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINER' with ID 99

  Scenario: Fail to retrieve a profile information when user is not found
    Given an authenticated user with a role 'TRAINER' and ID 1
    But the system cannot find trainer ID 1
    When the user sends a GET request to retrieve a trainer profile information for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainer not found"
    And the ownership verification is triggered for 'TRAINER' with ID 1


  Scenario: Update trainer profile successfully
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid update trainer profile request
    When the user sends a PUT request to update the trainer profile for ID 1
    Then the response status is 200 (OK)
    And the trainer summary is returned
    And the ownership verification is triggered for 'TRAINER' with ID 1
    And the update for ID 1 is processed by the trainer service

  Scenario Outline: Fail to update trainer profile due to validation errors
    Given an authenticated user with a role 'TRAINER' and ID 1
    And an update trainer profile request with invalid "<field_state>"
    When the user sends a PUT request to update the trainer profile for ID 1
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field         |
      | null full name        | fullName            |
      | blank first name      | fullName.firstName  |
      | blank last name       | fullName.lastName   |
      | null specialization   | specialization      |

  Scenario: Fail to update trainer profile when unauthenticated
    Given an anonymous user
    And a valid update trainer profile request
    When the user sends a PUT request to update the trainer profile for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to update the trainer profile for another user
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid update trainer profile request
    But the system rejects authorization for 'TRAINER' with ID 99
    When the user sends a PUT request to update the trainer profile for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINER' with ID 99

  Scenario: Fail to update trainer profile due to incorrect role
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid update trainer profile request
    When the user sends a PUT request to update the trainer profile for ID 1
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"

  Scenario: Fail to update the trainer profile when user is not found
    Given an authenticated user with a role 'TRAINER' and ID 1
    And a valid update trainer profile request
    But the system cannot find trainer ID 1
    When the user sends a PUT request to update the trainer profile for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainer not found"
    And the ownership verification is triggered for 'TRAINER' with ID 1


  Scenario: Change trainer activity status successfully
    Given an authenticated user with a role 'TRAINER' and ID 1
    When the user sends a PATCH request to change trainer activity status for ID 1
    Then the response status is 200 (OK)
    And the ownership verification is triggered for 'TRAINER' with ID 1
    And the activity status change for ID 1 is processed by the trainer service

  Scenario: Fail to change trainer activity status when unauthenticated
    Given an anonymous user
    When the user sends a PATCH request to change trainer activity status for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to change trainer activity status for another user
    Given an authenticated user with a role 'TRAINER' and ID 1
    But the system rejects authorization for 'TRAINER' with ID 99
    When the user sends a PATCH request to change trainer activity status for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'TRAINER' with ID 99

  Scenario: Fail to change trainer activity status due to incorrect role
    Given an authenticated user with a role 'TRAINEE' and ID 1
    When the user sends a PATCH request to change trainer activity status for ID 1
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Not enough rights to access the resource"

  Scenario: Fail to change trainer activity status when user is not found
    Given an authenticated user with a role 'TRAINER' and ID 1
    But the system cannot find trainer ID 1
    When the user sends a PATCH request to change trainer activity status for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "Trainer not found"
    And the ownership verification is triggered for 'TRAINER' with ID 1