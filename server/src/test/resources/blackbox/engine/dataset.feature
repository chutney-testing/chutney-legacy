# language: en
@Parameters
Feature: Replace scenario parameters with data set or global var values

Scenario: Replace scenario parameters with data set values
    Given a valid test case with a dataset
    When last saved scenario is executed
    Then the report status is SUCCESS


Scenario: Replace scenario parameters with global var values
    Given a valid test case with a parameter
    And global variables defined
    When last saved scenario is executed
    Then the report status is SUCCESS
