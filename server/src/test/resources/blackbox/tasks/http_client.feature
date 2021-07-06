# language: en
@HttpTask
Feature: HTTP Task test

    Scenario Outline: Http <verb> request wrong url
        Given A target pointing to an unknown http server
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "HTTP_ENV_<verb>_KO",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_http",
                            "url": "http://localhost:12345"
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
                    "title":"http client failure <verb>",
                    "scenario":{
                        "when":{
                            "sentence":"Make failed <verb> request",
                            "implementation":{
                                "task":"{\n type: http-${'<verb>'.toLowerCase()} \n target: test_http \n inputs: {\n <task_inputs> \n timeout: 500 ms \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Assert http status",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${T(Integer).toString(#status)} \n expected: 200 \n mode: not equals \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/HTTP_ENV_<verb>_KO
                With timeout 5 s
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals

        Examples:
            | verb    | task_inputs                       |
            | GET     | uri: /notused                     |
            | DELETE  | uri: /notused                     |
            | POST    | uri: /notused \n body: cool buddy |
            | PUT     | uri: /notused \n body: cool buddy |

    Scenario Outline: Http <verb> request local valid endpoint
        Given an app providing an http interface
            Do https-server-start
                With port ${#tcpPortRandomRange(100).toString()}
                With truststore-path ${#resourcePath("blackbox/keystores/truststore.jks")}
                With truststore-password truststore
                Take appServer ${#httpsServer}
        And a configured target for an endpoint
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "HTTP_ENV_<verb>",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_http",
                            "url": "${#appServer.baseUrl()}",
                            "keyStore": "${#escapeJson(#resourcePath("blackbox/keystores/client.jks"))}",
                            "keyStorePassword": "client"
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
                    "title":"http client <verb>",
                    "scenario":{
                        "when":{
                            "sentence":"Make <verb> request",
                            "implementation":{
                                "task":"{\n type: http-${'<verb>'.toLowerCase()} \n target: test_http \n inputs: {\n <task_inputs> \n timeout: 5000 ms \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Assert http status",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${T(Integer).toString(#status)} \n expected: \"200\" \n mode: equals \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/HTTP_ENV_<verb>
                With timeout 5 s
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

        Examples:
            | verb    | task_inputs                         |
            | GET     | uri: /mock/get                      |
            | DELETE  | uri: /mock/delete                   |
            | POST    | uri: /mock/post \n body: cool buddy |
            | PUT     | uri: /mock/put \n body: cool buddy  |
