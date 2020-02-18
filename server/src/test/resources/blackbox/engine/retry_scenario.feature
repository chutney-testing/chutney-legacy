# language: en
Feature: Execution retry strategy

Scenario: Retry should stop after success assertion
    Given this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "Test",
            "steps": [{
                "name": "Set stop date"
                "type": "context-put",
                "inputs": {
                    "entries": {
                        "dateTimeFormat": "ss",
                        "secondsPlus5": "${T(java.time.format.DateTimeFormatter).ofPattern(#dateTimeFormat).format(T(java.time.ZonedDateTime).now().plusSeconds(5))}"
                    }
                }
            },
            {
                "name": "Assertion",
                "strategy": {
                    "type": "retry-with-timeout",
                    "parameters": {
                        "timeOut": "15 sec",
                        "retryDelay": "1 sec"
                    }
                },
                "steps": [{
                    "name": "Set current date"
                    "type": "context-put",
                    "inputs": {
                        "entries": {
                            "currentSeconds": "${T(java.time.format.DateTimeFormatter).ofPattern(#dateTimeFormat).format(T(java.time.ZonedDateTime).now())}"
                        }
                    }
                },{
                    "name": "Check current date get to stop date"
                    "type": "string-assert",
                    "inputs": {
                        "document": "${#secondsPlus5}",
                        "expected": "${T(java.lang.String).format('%02d', new Integer(#currentSeconds) + 1)}"
                    }
                }]
            }]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS
