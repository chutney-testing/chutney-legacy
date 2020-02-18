# language: en
@Campaign
Feature:  Campaign execution

Background:
    Given a scenario with name "scenario1" is stored
      """
      {
          "scenario": {
              "name": "Success scenario",
              "steps": [
                  {
                    "name": "Just a success step",
                    "type": "success"
                  }
              ]
          }
      }
       """
    And a scenario with name "scenario2" is stored
      """
      {
          "scenario": {
              "name": "Success scenario",
              "steps": [
                  {
                    "name": "Just a success step",
                    "type": "success"
                  }
              ]
          }
      }
       """

Scenario: Execution by campaign id with 2 scenarios

    Given a campaign with name "campaign2" is stored with the following scenarios :
            |scenario1|
            |scenario2|

    When this campaign is executed by id
    Then the execution report is returned
    And this execution report is stored in the campaign execution history

Scenario: Execution by campaign name with 1 scenario

    Given a campaign with name "campaign1" is stored with the following scenarios :
        |scenario1|

    When this campaign is executed by name
    Then the execution reports are returned
    And this execution report is stored in the campaign execution history

Scenario: Execution for surefire of a campaign with 1 scenario
    Given a campaign with name "campaign1" is stored with the following scenarios :
        |scenario1|
    When this campaign is executed for surefire
    Then the execution is SUCCESS

Scenario: Execution of an unknown campaign
    When an unknown campaign is executed by id
    Then the campaign is not found

Scenario: Execution of an unknown named campaign
    When an unknown campaign is executed by name
    Then the campaign report is empty
