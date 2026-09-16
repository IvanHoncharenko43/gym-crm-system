Feature: Consume Trainer Workload
  As a trainer-workload-service
  I want to receive and process asynchronous events on trainer workload
  So that I can provide users with up-to-date statistics

  Scenario: Consume trainer workload to create a new record
    Given a trainer workload update event for base username "John.Doe", year 2026 and month "May" with duration 90 minutes
    When a trainer workload update event is sent
    Then a new workload is created for given username

  Scenario: Consume trainer workload to update an existing record
    Given a workload data for trainer with base username "John.Doe" already exists for year 2026 and month "May" with duration 90 minutes
    And a trainer workload update event for base username "John.Doe", year 2026 and month "June" with duration 60 minutes
    When a trainer workload update event is sent
    Then an existing workload for given username with month "May" and year 2026 data is updated

  Scenario: Consume trainer workload with invalid json
    Given a trainer workload update event for base username "Error.Trainer" with invalid JSON format
    When a trainer workload update event is sent
    Then the invalid message is routed to the trainer workload update Dead Letter Topic
