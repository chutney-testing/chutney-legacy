# language: en
Feature: Amqp Task test

Scenario: amqp test all steps
    Given an embedded amqp server
    And a target test_amqp with url amqp://localhost:5672 with security
    |username|guest|
    |password|guest|
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
                    "name": "Create queue",
                    "type": "amqp-create-bound-temporary-queue",
                    "target": "test_amqp",
                    "inputs": {
                        "exchange-name": "amq.direct",
                        "routing-key": "routemeplease",
                        "queue-name": "test"
                    }
                },{
                    "name": "Publish message 1",
                    "type": "amqp-basic-publish",
                    "target": "test_amqp",
                    "inputs": {
                        "exchange-name": "amq.direct",
                        "routing-key": "routemeplease",
                        "payload": "bodybuilder"
                    }
                },{
                    "name": "Publish message 2",
                    "type": "amqp-basic-publish",
                    "target": "test_amqp",
                    "inputs": {
                        "exchange-name": "amq.direct",
                        "routing-key": "routemeplease",
                        "payload": "bodybuilder2"
                    }
                },{
                    "name": "Get messages",
                    "type": "amqp-basic-get",
                    "target": "test_amqp",
                    "inputs": {
                        "queue-name": "test"
                    }
                },{
                    "name": "Check message 1",
                    "type": "string-assert",
                    "inputs": {
                        "document": "${#body}",
                        "expected": "bodybuilder"
                   }
                }, {
                    "name": "Clean queue",
                    "type": "amqp-clean-queues",
                    "target": "test_amqp",
                    "inputs": {
                        "queue-names": ["test"]
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
