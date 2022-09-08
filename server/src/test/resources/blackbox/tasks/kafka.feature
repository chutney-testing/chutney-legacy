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
                Validate httpStatusCode_200 ${#status == 200}
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
                                "action":"{\n type: kafka-basic-publish \n target: test_kafka \n inputs: {\n topic: a-topic \n payload: bodybuilder \n} \n}"
                            }
                        },
                        "thens":[]
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/KAFKA_ENV_KO
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
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
                Validate httpStatusCode_200 ${#status == 200}
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
                                "action":"{\n type: kafka-basic-publish \n target: test_kafka \n inputs: {\n topic: a-topic \n payload: bodybuilder \n headers: {\n X-API-VERSION: \"1.0\" \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Consume from broker",
                                "implementation":{
                                    "action":"{\n type: kafka-basic-consume \n target: test_kafka \n inputs: {\n topic: a-topic \n group: chutney \n ackMode: BATCH \n properties: {\n auto.offset.reset: earliest \n} \n} \n outputs: {\n payload : \${#payloads[0]} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check payload",
                                "implementation":{
                                    "action":"{\n type: string-assert \n inputs: {\n document: \${#payload} \n expected: bodybuilder \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/KAFKA_ENV_OK
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario Outline: Kafka basic one publish / two consumes with ackMode <ackMode>
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
                    "name": "KAFKA_ENV_<testEnvName>",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_kafka",
                            "url": "tcp://${#kafkaBroker.getBrokerAddresses()[0]}",
                            "properties": [
                                { "key": "ackMode", "value": "<ackMode>" },
                                { "key": "auto.offset.reset", "value": "earliest" },
                                { "key": "auto.commit.count", "value": "1" }
                            ]
                        }
                    ]
                }
                """
                Validate httpStatusCode_200 ${#status == 200}
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
                                "action":"{\n type: kafka-basic-publish \n target: test_kafka \n inputs: {\n topic: a-topic \n payload: bodybuilder \n headers: {\n X-API-VERSION: \"1.0\" \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Consume from broker",
                                "implementation":{
                                    "action":"{\n type: kafka-basic-consume \n target: test_kafka \n inputs: {\n topic: a-topic \n group: chutney \n} \n outputs: {\n payload : \${#payloads[0]} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check payload",
                                "implementation":{
                                    "action":"{\n type: string-assert \n inputs: {\n document: \${#payload} \n expected: bodybuilder \n} \n}"
                                }
                            },
                            {
                                "sentence":"Consume from broker the same message again",
                                "implementation":{
                                    "action":"{\n type: kafka-basic-consume \n target: test_kafka \n inputs: {\n topic: a-topic \n group: chutney \n timeout: 3 s \n} \n outputs: {\n payloadBis : \${#payloads[0]} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check payload",
                                "implementation":{
                                    "action":"{\n type: string-assert \n inputs: {\n document: \${#payloadBis} \n expected: bodybuilder \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/KAFKA_ENV_<testEnvName>
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is <reportStatus>
            Do compare Global status
                With actual ${#json(#report, "$.report.status")}
                With expected <reportStatus>
                With mode equals
            Do compare Second consume step status
                With actual ${#json(#report, "$.report.steps[?(@.name=='Consume from broker the same message again')].status").get(0)}
                With expected <reportStatus>
                With mode equals

        Examples:
            | ackMode          | reportStatus | testEnvName    |
            | MANUAL           | SUCCESS      | MANUAL         |
            | MANUAL_IMMEDIATE | SUCCESS      | MANUAL_IMM     |
            | RECORD           | FAILURE      | RECORD         |
            | TIME             | FAILURE      | TIME           |
            | COUNT            | FAILURE      | COUNT          |
            | COUNT_TIME       | FAILURE      | COUNT_TIME     |
            | BATCH            | FAILURE      | BATCH          |
