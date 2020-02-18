# language: en
Feature: Execution success task

Scenario: Task instantiation and execution of a success scenario
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
                    "name": "step success",
                    "type": "success"
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS

Scenario: Task instantiation and execution of a failed scenario
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
            "name": "Failed scenario",
            "steps":
            [
                {
                    "name": "step fail",
                    "type": "fail"
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE

Scenario: Task instantiation and execution of a sleep scenario
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
            "name": "Sleep scenario",
            "steps":
            [
                {
                    "name": "step sleep",
                    "type": "sleep",
                    "inputs": {
                        "duration": "20 ms"
                    }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS

Scenario: Task instantiation and execution of a debug scenario
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
            "name": "Debug scenario",
            "steps":
            [
                {
                "name": "Put value in context",
                "type": "context-put",
                "inputs": {
                    "entries": {
                        "test key" : "valeur"
                    }
                }
            },

                {
                    "type": "debug"
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS
