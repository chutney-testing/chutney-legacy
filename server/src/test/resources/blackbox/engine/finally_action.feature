# language: en

Feature: Finally actions

Scenario: Step of a type self registering as Finally Action does not create an infinite loop
    Given this scenario is saved
    """
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario":
        {
            "name": "Success scenario",
            "steps":
            [
                {
                    "name": "Do something to register finally action",
                    "type": "self-registering-finally"
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS
