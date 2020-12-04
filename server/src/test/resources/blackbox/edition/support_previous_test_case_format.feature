# language: en
@Edition
Feature: Support previous test case format
# TODO - rename step `description` to `sentence` and remove difference between http API & raw DTO

Scenario Outline: Save or Update a test case using raw edition with an old format
    When saving a test case with a raw <scenario> written in an old format
    Then the persisted test case is converted to the last format

    Examples:
    | scenario                          |
    | scenario.v0.json                  |
    | scenario.v0.hjson                 |
    | scenario.v1.json                  |
    | scenario.v1.hjson                 |
    | raw_scenario_json_task.v2.0.json  |
    | raw_scenario_hjson_task.v2.0.json |


Scenario Outline: Consult an existing scenario (old format) using GWT form view
    Given an existing <scenario> written in format <version>
    When it is retrieved
    Then it is viewed in the new format
    But still persisted in its original format <version>

    Examples:
        | scenario                          | version |
        | scenario.v0.json                  | v0.0    |
        | scenario.v0.hjson                 | v0.0    |
        | scenario.v1.json                  | v1.0    |
        | scenario.v1.hjson                 | v1.0    |
        | raw_scenario_json_task.v2.0.json  | v2.0    |
        | raw_scenario_hjson_task.v2.0.json | v2.0    |


Scenario Outline: Consult an existing scenario using RAW view
    Given an existing <scenario> written in format <version>
    When it is retrieved as raw
    Then it is viewed as raw

    Examples:
        | scenario                          | version |
        | scenario.v0.json                  | v0.0    |
        | scenario.v0.hjson                 | v0.0    |
        | scenario.v1.json                  | v1.0    |
        | scenario.v1.hjson                 | v1.0    |
        | raw_scenario_json_task.v2.0.json  | v2.0    |
        | raw_scenario_hjson_task.v2.0.json | v2.0    |


Scenario Outline: Execute an existing scenario (old format)
    Given an existing <scenario> written in format <version>
    When last saved scenario is executed
    Then the report status is SUCCESS

    Examples:
        | scenario                                 | version |
        | scenario.v0.json                         | v0.0    |
        | scenario.v0.hjson                        | v0.0    |
        | scenario.v1.json                         | v1.0    |
        | scenario.v1.hjson                        | v1.0    |
        | scenario_executable_json_task.v2.0.json  | v2.0    |
        | scenario_executable_hjson_task.v2.0.json | v2.0    |
        | scenario_executable.v2.1.json            | v2.1    |


Scenario Outline: Execute existing scenario containing errors
    Given an existing <scenario> written in format <version>
    When last saved scenario execution produces an error
    Then the error status is <status> with message <message>

    Examples:
        | version | scenario                          | status | message                                                                                                                  |
        | v0.0    | scenario_unknown_target.v0.hjson  | 500    | com.chutneytesting.environment.domain.TargetNotFoundException: Target [UNKNOWN_TARGET] not found in environment [GLOBAL] |


# TODO - bug on sql task still relevant ?
#Scenario Outline: Save and get sql scenario
#    Given saving a test case with a raw <scenario> written in an old format
#    When it is retrieved
#    And it is retrieved as raw
#    Then the persisted test case is converted to the last format
#    And it is viewed in the new format
#
#    Examples:
#        |   scenario                    |
#        |   scenario_docstring.v0.hjson |


Scenario Outline: Task implementation is always HJSON readable
    When saving a test case with a <scenario> written with GWT form
    Then the task implementation is HJSON readable

    Examples:
        | scenario                      |
        | scenario_json_task.v2.0.json  |
        | scenario_hjson_task.v2.0.json |
        # TODO - v2.1 is not in use in web UI, yet
        | scenario.v2.1.json            |

