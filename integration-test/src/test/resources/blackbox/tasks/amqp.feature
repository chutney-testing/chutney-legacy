# language: en
Feature: Amqp Task test

    Scenario: amqp test all steps
        Given An embedded amqp server
            Do qpid-server-start
        And A target for this server
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "AMQP_SCENARIO_ENV",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_amqp",
                            "url": "amqp://localhost:5672",
                            "username": "guest",
                            "password": "guest"
                        }
                    ]
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
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
                    "title":"AMQP scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Create queue",
                            "implementation":{
                                "task":"{\n type: amqp-create-bound-temporary-queue \n target: test_amqp \n inputs: {\n exchange-name: amq.direct \n routing-key: routemeplease \n queue-name: test \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Publish message 1",
                                "implementation":{
                                    "task":"{\n type: amqp-basic-publish \n target: test_amqp \n inputs: {\n exchange-name: amq.direct \n routing-key: routemeplease \n payload: bodybuilder \n} \n}"
                                }
                            },
                            {
                                "sentence":"Publish message 2",
                                "implementation":{
                                    "task":"{\n type: amqp-basic-publish \n target: test_amqp \n inputs: {\n exchange-name: amq.direct \n routing-key: routemeplease \n payload: bodybuilder2 \n} \n}"
                                }
                            },
                            {
                                "sentence":"Get messages",
                                "implementation":{
                                    "task":"{\n type: amqp-basic-get \n target: test_amqp \n inputs: {\n queue-name: test \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check message 1",
                                "implementation":{
                                    "task":"{\n type: string-assert \n inputs: {\n document: \${#body} \n expected: bodybuilder \n} \n}"
                                }
                            },
                            {
                                "sentence":"Clean queue",
                                "implementation":{
                                    "task":"{\n type: amqp-clean-queues \n target: test_amqp \n inputs: {\n queue-names: [\n test \n] \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/AMQP_SCENARIO_ENV
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
