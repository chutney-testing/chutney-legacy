# language: en
@Jms
Feature: Jms Task test

Scenario: Jms sender wrong url
    Given a target test with url tcp://localhost:12345 with properties
    | java.naming.factory.initial | org.apache.activemq.jndi.ActiveMQInitialContextFactory |
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "jms client failure sender",
            "steps": [
                {
                   "name": "Send JMS message",
                   "type":"jms-sender",
                   "target": "test",
                   "inputs":{
                      "destination": "test",
                      "body": "some"
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

Scenario: Jms clean queue wrong url
    Given a target test with url tcp://localhost:12345 with properties
    | java.naming.factory.initial | org.apache.activemq.jndi.ActiveMQInitialContextFactory |
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "jms client failure sender",
            "steps": [
                {
                   "name": "Clean queue",
                   "type":"jms-clean-queue",
                   "target": "test",
                   "inputs":{
                       "destination":"pouet",
                       "bodySelector":"test"
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

Scenario: Jms listener queue wrong url
    Given a target test with url tcp://localhost:12345 with properties
    | java.naming.factory.initial | org.apache.activemq.jndi.ActiveMQInitialContextFactory |
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "jms listener failure sender",
            "steps": [
                {
                   "name": "Listen queue",
                   "type":"jms-listener",
                   "target": "test",
                   "inputs":{
                       "destination":"pouet",
                       "bodySelector":"test"
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

Scenario: Jms sender then clean then send and listen it on embedded broker
    Given a dynamic target test_jms with url vm://localhost with properties
    | java.naming.factory.initial | org.apache.activemq.jndi.ActiveMQInitialContextFactory |
    And a mock jms endpoint with reference port test_jms_port with host localhost
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "jms client sender the clean",
            "steps": [
                {
                   "name": "Send JMS message",
                   "type":"jms-sender",
                   "target": "test_jms",
                   "inputs":{
                      "destination": "dynamicQueues/test",
                      "body": "some"
                   }
                },
                {
                   "name": "Clean queue",
                   "type":"jms-clean-queue",
                   "target": "test_jms",
                   "inputs":{
                      "destination": "dynamicQueues/test"
                   }
                },
                {
                   "name": "Send JMS message",
                   "type":"jms-sender",
                   "target": "test_jms",
                   "inputs":{
                      "destination": "dynamicQueues/test",
                      "body": "message to catch"
                   }
                },
                {
                   "name": "Listen to queue",
                   "type":"jms-listener",
                   "target": "test_jms",
                   "inputs":{
                      "destination": "dynamicQueues/test"
                   }
                },
                {
                    "name": "Check JMS message",
                    "type": "string-assert",
                    "inputs": {
                        "document": "${#textMessage}",
                        "expected": "message to catch"
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
