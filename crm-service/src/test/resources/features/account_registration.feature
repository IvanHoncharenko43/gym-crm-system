Feature: Account Registration
  As an unauthenticated user
  I want to be able to register as a trainee or a trainer
  So that I can get access to finding coaches or clients

  Scenario: Register a new trainee with all parameters successfully
    Given an anonymous user
    And a valid trainee registration request with all parameters
    When the user sends a POST request to register a trainee
    Then the response status is 201 (Created)
    And the trainee summary is returned
    And the trainee registration is processed by the trainee service

  Scenario: Register a new trainee with only required parameters successfully
    Given an anonymous user
    And a valid trainee registration request with only required parameters
    When the user sends a POST request to register a trainee
    Then the response status is 201 (Created)
    And the trainee summary is returned
    And the trainee registration is processed by the trainee service

  Scenario Outline: Fail to register a trainee due to validation errors
    Given an anonymous user
    And a trainee registration request with invalid "<field_state>"
    When the user sends a POST request to register a trainee
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field         |
      | null full name        | fullName            |
      | blank first name      | fullName.firstName  |
      | blank last name       | fullName.lastName   |
      | underage birth date   | dateOfBirth         |
      | address too long      | address             |


  Scenario: Register a new trainer successfully
    Given an anonymous user
    And a valid trainer registration request
    When the user sends a POST request to register a trainer
    Then the response status is 201 (Created)
    And the trainer summary is returned
    And the trainer registration is processed by the trainer service

  Scenario Outline: Fail to register a trainer due to validation errors
    Given an anonymous user
    And a trainer registration request with invalid "<field_state>"
    When the user sends a POST request to register a trainer
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field         |
      | null full name        | fullName            |
      | blank first name      | fullName.firstName  |
      | blank last name       | fullName.lastName   |
      | null specialization   | specialization      |