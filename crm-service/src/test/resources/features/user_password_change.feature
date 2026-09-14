Feature: User Password Management
  As a registered user
  I want to be able to change my password

  Scenario: Change password successfully
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid change password request
    When the user sends a PUT request to change the password for ID 1
    Then the response status is 200 (OK)
    And the ownership verification is triggered for 'USER' with ID 1
    And the password change for ID 1 is processed by the user service

  Scenario Outline: Fail to change password due to validation errors
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a change password request with old password "<old_password>" and new password "<new_password>"
    When the user sends a PUT request to change the password for ID 1
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | old_password    | new_password     | error_field |
      |                 | newPassword1234! | oldPassword |
      | oldPassword1234 |                  | newPassword |
      | oldPassword1234 | short            | newPassword |

  Scenario: Fail to change password when unauthenticated
    Given an anonymous user
    And a valid change password request
    When the user sends a PUT request to change the password for ID 1
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"

  Scenario: Fail to change password for another user
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid change password request
    But the system rejects authorization for 'USER' with ID 99
    When the user sends a PUT request to change the password for ID 99
    Then the response status is 403 (Forbidden)
    And the response is a ProblemDetail with title "Authorization Failure" and detail "Authorization failed"
    And the ownership verification is triggered for 'USER' with ID 99

  Scenario: Fail to change password when user is not found
    Given an authenticated user with a role 'TRAINEE' and ID 1
    And a valid change password request
    But the system cannot find user ID 1
    When the user sends a PUT request to change the password for ID 1
    Then the response status is 404 (Not Found)
    And the response is a ProblemDetail with title "Entity Not Found" and detail "User not found"
    And the ownership verification is triggered for 'USER' with ID 1