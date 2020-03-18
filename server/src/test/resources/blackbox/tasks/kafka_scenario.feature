# language: en
Feature: Kafka all Tasks test

Scenario: kafka basic publish success
    Given an embedded kafka server with a topic mon-topic
    And a consumer listening the kafka topic mon-topic
    And an existing target test_kafka having url in system property spring.embedded.kafka.brokers
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
            "name": "kafka client sender",
            "steps": [
               {
                   "name": "Publish to broker",
                    "type": "kafka-basic-publish",
                    "target": "test_kafka",
                    "inputs": {
                        "topic": "mon-topic",
                        "payload": "bodybuilder",
                        "headers": {
                            "X--API-VERSION": "1.0"
                        }
                    }
                },
               {
                 "name": "Consume from broker",
                  "type": "kafka-basic-consume",
                  "target": "test_kafka",
                  "inputs": {
                      "topic": "mon-topic",
                      "group": "chutney"
                  },
                  "outputs": {
                     "payload" : "${#payloads[0]}"
                  }
                },
               {
                   "name": "Check Payload"
                   "type": "string-assert",
                   "inputs": {
                       "document": "${#payload}",
                       "expected": "bodybuilder"
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
    And the message payload bodybuilder is well produced

Scenario: kafka basic publish wrong url failure
    Given an embedded kafka server with a topic mon-topic
    And a consumer listening the kafka topic mon-topic
    And an existing target test_kafka with url tcp://wrong-url:5555
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
        "name": "kafka client sender",
        "steps": [
           {
                "name": "Publish to broker",
                "type": "kafka-basic-publish",
                "target": "test_kafka",
                "inputs": {
                    "topic": "mon-topic",
                    "payload": "bodybuilder"
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
