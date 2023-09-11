# language: en
@jakarta
Feature: jakarta Task test

    Scenario Outline: Jakarta <jakarta-action-id> wrong url
        Given A target pointing to an unknown service
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environments
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "JAKARTA_${'<jakarta-action-id>'.toUpperCase()}_KO",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_jakarta",
                            "url": "tcp://localhost:12345",
                            "properties": [
                                {
                                    "key": "java.naming.factory.initial",
                                    "value": "org.apache.activemq.jndi.ActiveMQInitialContextFactory"
                                }
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
                    "title":"jakarta client failure <jakarta-action-id>",
                    "scenario":{
                        "when":{
                            "sentence":"Make failed jakarta request",
                            "implementation":{
                                "task":"{\n type: jakarta-<jakarta-action-id> \n target: test_jakarta \n inputs: {\n <action_inputs> \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Assert http status",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#status} \n expected: 200 \n mode: not equals \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/JAKARTA_${'<jakarta-action-id>'.toUpperCase()}_KO
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals

        Examples:
            | jakarta-action-id | action_inputs                                 |
            | sender      | destination: test \n body: something        |
            | clean | destination: test \n bodySelector: selector |
            | listener    | destination: test \n bodySelector: selector |

    Scenario: jakarta sender then clean then send and listen it on embedded broker
        Given a jakarta endpoint
            Do jakarta-broker-start
                With config-uri tcp://localhost:61616
        And an associated target
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environments
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "JAKARTA_ENV_OK",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_jakarta",
                            "url": "tcp://localhost:61616",
                            "properties": [
                                {
                                    "key": "java.naming.factory.initial",
                                    "value": "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
                                }
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
                    "title":"jakarta actions scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Send jakarta Message",
                            "implementation":{
                                "task":"{\n type: jakarta-sender \n target: test_jakarta \n inputs: {\n destination: dynamicQueues/test \n body: something \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Clean queue",
                                "implementation":{
                                    "task":"{\n type: jakarta-clean-queue \n target: test_jakarta \n inputs: {\n destination: dynamicQueues/test \n} \n}"
                                }
                            },
                            {
                                "sentence":"Send jakarta Message",
                                "implementation":{
                                    "task":"{\n type: jakarta-sender \n target: test_jakarta \n inputs: {\n destination: dynamicQueues/test \n body: message to catch \n} \n}"
                                }
                            },
                            {
                                "sentence":"Listen to queue",
                                "implementation":{
                                    "task":"{\n type: jakarta-listener \n target: test_jakarta \n inputs: {\n destination: dynamicQueues/test \n} \n}"
                                }
                            },
                            {
                                "sentence":"Check jakarta message",
                                "implementation":{
                                    "task":"{\n type: string-assert \n inputs: {\n document: \${#textMessage} \n expected: message to catch \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/JAKARTA_ENV_OK
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
