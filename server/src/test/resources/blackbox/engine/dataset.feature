# language: en
@Parameters
Feature: Replace scenario parameters with data set or global var values

    Background:
        Given global variables defined in global_var

    Scenario: Execute raw scenario with global vars
        Given an existing testcase testcase_for_global_vars.v1.json written in old format
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Execute gwt scenario with global vars
        Given a testcase testcase_for_global_vars.v2.1.json written with GWT form
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Execute composable testcase with global vars
        Given composable task components
            | context-put |
            | assert-equals |
        And a composable testcase composable-testcase_for_global_vars
        When last saved scenario is executed
        Then the report status is SUCCESS
