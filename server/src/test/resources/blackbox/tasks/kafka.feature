# language: en
@Kafka
Feature: Kafka all Tasks test

    Scenario: Kafka basic publish wrong url failure
        Given A target pointing to a non unknown service
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "KAFKA_ENV_KO",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_kafka",
                            "url": "tcp://unknownhost:12345"
                        }
                    ]
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        And this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"kafka client sender",
                    "scenario":{
                        "when":{
                            "sentence":"Publish to broker",
                            "implementation":{
                                "task":"{\n type: kafka-basic-publish \n target: test_kafka \n inputs: {\n topic: a-topic \n payload: bodybuilder \n} \n}"
                            }
                        },
                        "thens":[]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/KAFKA_ENV_KO
                With timeout 5 s
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        Then the report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals

    Scenario: Kafka basic publish success
        Given an embedded kafka server with a topic a-topic
            Do kafka-broker-start
                With topics
                | a-topic |
        And an associated target test_kafka having url in system property spring.embedded.kafka.brokers
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "KAFKA_ENV_OK",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_kafka",
                            "url": "tcp://${#kafkaBroker.getBrokerAddresses()[0]}"
                        }
                    ]
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        And this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"kafka client sender",
                    "scenario":{
                        "when":{
                            "sentence":"Publish to broker",
                            "implementation":{
                                "task":"{\n type: kafka-basic-publish \n target: test_kafka \n inputs: {\n topic: a-topic \n payload: bodybuilder \n headers: {\n X-API-VERSION: \"1.0\" \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Consume from broker",
                                "implementation":{
                                    "task":"{\n type: kafka-basic-consume \n target: test_kafka \n inputs: {\n topic: a-topic \n group: chutney \n properties: {\n auto.offset.reset: earliest \n} \n} \n outputs: {\n payload : \${#payloads[0]} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check payload",
                                "implementation":{
                                    "task":"{\n type: string-assert \n inputs: {\n document: \${#payload} \n expected: bodybuilder \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/KAFKA_ENV_OK
                With timeout 5 s
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
