# language: en
Feature: Execution fault barrier catch error

    Scenario: Task instantiation and execution with error
    Given a locally executable task "test" needing no parameters
    And task failed due to uncatch exception with message "Error Exception"
    When the following scenario is executed
    """
      {
          "name": "Single step test",
          "type": "test"
      }
    """
    Then the task "test" is executed
    And the execution status of the task is KO
    And execution report has error reported in with message "Error Exception"
    And no error is raise
