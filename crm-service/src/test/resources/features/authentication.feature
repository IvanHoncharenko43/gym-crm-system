Feature: System Authentication
  As a registered user
  I want to log in quickly and securely
  So that I can continue my work where I left off without burden

  Scenario: Successfully log in and receive a token
    Given an anonymous user
    And a valid login request with correct credentials
    When the user sends a POST request to log in
    Then the response status is 200 (OK)
    And the login details containing a token are returned
    And the login is processed by the auth service

  Scenario Outline: Fail to log in due to request validation errors
    Given an anonymous user
    And a login request with invalid "<field_state>"
    When the user sends a POST request to log in
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field |
      | blank username        | username    |
      | blank password        | password    |

  Scenario: Fail to log in due to invalid credentials
    Given an anonymous user
    And a valid login request with correct credentials
    But the system rejects the credentials
    When the user sends a POST request to log in
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Invalid username or password"

  Scenario: Fail to log in because the account is temporarily locked
    Given an anonymous user
    And a valid login request with correct credentials
    But the system flags the account as temporarily locked
    When the user sends a POST request to log in
    Then the response status is 429 (Too Many Requests)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Account is temporarily locked due to too many failed login attempts"

  Scenario: Fail to log in because the account is inactive
    Given an anonymous user
    And a valid login request with correct credentials
    But the system flags the account as inactive
    When the user sends a POST request to log in
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "The account is inactive"


  Scenario: Successfully log out and blacklist the token
    Given an authenticated user with a valid bearer token
    When the user sends a POST request to log out
    Then the response status is 200 (OK)
    And the logout is processed by the auth service

  Scenario: Fail to log out when unauthenticated
    Given an anonymous user
    When the user sends a POST request to log out
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"