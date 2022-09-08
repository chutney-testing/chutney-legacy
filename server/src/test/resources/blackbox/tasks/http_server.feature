# language: en
@HttpServerTask
Feature: HTTP server Task test

    Scenario: Http post request local server endpoint
        Given a target for the http server mock
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "HTTP_SERVER_ENV",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_http",
                            "url": "https://localhost:8443",
                            "properties": [
                                { "key" : "keyStore", "value": "${#escapeJson(#resourcePath("blackbox/keystores/client.jks"))}" },
                                { "key" : "keyStorePassword", "value": "client" },
                                { "key" : "keyPassword", "value": "client" }
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
                    "title":"http client post",
                    "scenario":{
                        "givens":[
                            {
                                "sentence":"Start HTTPS server",
                                "implementation":{
                                    "action":"{\n type: https-server-start \n inputs: {\n port: \"8443\" \n truststore-path: \${#resourcePath('blackbox/keystores/truststore.jks')} \n truststore-password: truststore \n}\n}"
                                }
                            }
                        ],
                        "when":{
                            "sentence":"Make POST request",
                            "implementation":{
                                "action":"{\n type: http-post \n target: test_http \n inputs: {\n uri: /test \n body: cool buddy \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Listens to POST requests",
                                "implementation":{
                                    "action":"{\n type: https-listener \n inputs: {\n https-server: \${#httpsServer} \n uri: /test \n verb: POST \n expected-message-count: \"1\" \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/HTTP_SERVER_ENV
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
