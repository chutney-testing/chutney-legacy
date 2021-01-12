# language: en
@SSH
Feature: SSHd Task test

Scenario: Scenario execution with simple ssh task
    Given an SSHD server is started
    And Target test containing SSHD connection information
    And this scenario is saved
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
            "name": "ssh task scenario",
            "steps":
            [
                {
                   "name": "Execute echo command",
                   "type":"ssh-client",
                   "target": "test",
                   "inputs":{
                      "commands": [
                        "echo test"
                      ]
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
    And the output resulting context is
"""
[{"command":{"command":"echo test","timeout":{"durationValue":5000.0,"durationUnit":"MILLIS"}},"exitCode":0,"stdout":"test\n","stderr":""}]
"""

Scenario: Scenario execution with multiple ssh task
    Given an SSHD server is started
    And Target test2 containing SSHD connection information
    And this scenario is saved
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
            "name": "ssh task scenario",
            "steps":
            [
                {
                   "name": "Execute commands",
                   "type":"ssh-client",
                   "target": "test2",
                   "inputs":{
                      "commands": [
                        {
                            "command": "echo test",
                            "timeout": "500 ms"
                        },
                        {
                            "command": "echo test2"
                        }
                      ]
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
    And the output resulting context is
"""
[{"command":{"command":"echo test","timeout":{"durationValue":500.0,"durationUnit":"MILLIS"}},"exitCode":0,"stdout":"test\n","stderr":""},{"command":{"command":"echo test2","timeout":{"durationValue":5000.0,"durationUnit":"MILLIS"}},"exitCode":0,"stdout":"test2\n","stderr":""}]
"""

Scenario: Scenario execution unable to login, status SUCCESS and command stderr
    Given an SSHD server is started
    And Target testWrong containing SSHD connection information with wrong password
    And this scenario is saved
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
            "name": "ssh task scenario",
            "steps":
            [
                {
                   "name": "Execute echo command",
                   "type":"ssh-client",
                   "target": "testWrong",
                   "inputs":{
                      "commands": ["echo test"]
                   }
                }
            ]
        }
    }
'''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE
