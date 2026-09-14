Feature: Training Reporting
  As a registered user
  I want to search my trainings history and trainers' workload summaries
  So that I can monitor my fitness schedule and information about trainers' workloads

  Scenario: Retrieve a trainer's trainings successfully with all parameters
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    And a valid trainer training search request with all parameters
    And valid trainer training records exist for "Trainer.Name"
    When the user sends a POST request to search trainer trainings
    Then the response status is 200 (OK)
    And the trainings list is returned
    And the ownership verification is triggered for username "Trainer.Name"
    And the trainer training search is processed by the training service

  Scenario: Retrieve a trainer's trainings successfully with only required parameters
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    And a valid trainer training search request with only required parameters
    And valid trainer training records exist for "Trainer.Name"
    When the user sends a POST request to search trainer trainings
    Then the response status is 200 (OK)
    And the trainings list is returned
    And the ownership verification is triggered for username "Trainer.Name"
    And the trainer training search is processed by the training service

  Scenario Outline: Fail to search trainer trainings due to validation errors
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    And a trainer training search request with invalid "<field_state>"
    When the user sends a POST request to search trainer trainings
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field     |
      | blank username        | username        |
      | inverted date range   | dateRangeValid  |
      | trainee name too long | traineeName     |

  Scenario: Fail to search trainer trainings when unauthenticated
    Given an anonymous user
    And a valid trainer training search request with all parameters
    When the user sends a POST request to search trainer trainings
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"


  Scenario: Retrieve a trainee's trainings successfully with all parameters
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    And a valid trainee training search request with all parameters
    And valid trainee training records exist for "John.Doe"
    When the user sends a POST request to search trainee trainings
    Then the response status is 200 (OK)
    And the trainings list is returned
    And the ownership verification is triggered for username "John.Doe"
    And the trainee training search is processed by the training service

  Scenario: Retrieve a trainee's trainings successfully with only required parameters
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    And a valid trainee training search request with only required parameters
    And valid trainee training records exist for "John.Doe"
    When the user sends a POST request to search trainee trainings
    Then the response status is 200 (OK)
    And the trainings list is returned
    And the ownership verification is triggered for username "John.Doe"
    And the trainee training search is processed by the training service

  Scenario Outline: Fail to search trainee trainings due to validation errors
    Given an authenticated user with a role 'TRAINEE' and username "John.Doe"
    And a trainee training search request with invalid "<field_state>"
    When the user sends a POST request to search trainee trainings
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state           | error_field     |
      | blank username        | username        |
      | inverted date range   | dateRangeValid  |
      | trainer name too long | trainerName     |

  Scenario: Fail to search trainee trainings when unauthenticated
    Given an anonymous user
    And a valid trainee training search request with all parameters
    When the user sends a POST request to search trainee trainings
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"


  Scenario: Retrieve a trainer's monthly workload successfully
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    And a valid trainer workload summary exists for "Trainer.Name"
    When the user sends a GET request to retrieve the monthly workload with valid parameters
    Then the response status is 200 (OK)
    And the trainer workload summary is returned
    And the workload retrieval is processed by the trainer workload service

  Scenario Outline: Fail to retrieve a trainer's monthly workload due to validation errors
    Given an authenticated user with a role 'TRAINER' and username "Trainer.Name"
    When the user sends a GET request to retrieve the monthly workload with invalid "<field_state>"
    Then the response status is 400 (Bad Request)
    And the response is a ProblemDetail with title "Bad Request" containing a validation error for "<error_field>"

    Examples:
      | field_state                 | error_field    |
      | blank username              | username       |
      | negative year               | year           |
      | month out of bounds (13)    | month          |

  Scenario: Fail to retrieve a trainer's monthly workload when unauthenticated
    Given an anonymous user
    When the user sends a GET request to retrieve the monthly workload with valid parameters
    Then the response status is 401 (Unauthorized)
    And the response is a ProblemDetail with title "Authentication Failure" and detail "Authentication token is missing"