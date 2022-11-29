# language: en
@Agent
Feature: Agent test

Agent A (main) -> Agent B
We are the agent B. The agent A send us its network configuration in order we save it.
// TODO - This test is flaky on some setup do the exploration process which is very time consuming, because of connection refused and connection timeout.
// In order to run test features in any order and then rename folder "1-agent" to "agent", when have to implement a way to delete all existing environment. 
// Current solution seems to be a for loop.

    Scenario: We receive a network configuration to persist
        Given network configuration initialized
            Do http-get Request current network configuration
                On CHUTNEY_LOCAL
                With uri /api/v1/description
                Take networkConfiguration ${#json(#body, "$.networkConfiguration")}
            Do http-post Init configuration
                On CHUTNEY_LOCAL
                With uri /api/v1/agentnetwork/configuration
                With timeout 5 sec
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body ${#jsonSerialize(#networkConfiguration)}
                Take networkConfiguration ${#body}
        When network configuration with target fake_target with url http://fake_url:1234/fake is received
            Do http-post
                On CHUTNEY_LOCAL
                With uri /api/v1/agentnetwork/wrapup
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "networkConfiguration": {
                        "creationDate": "${#jsonPath(#networkConfiguration, "$.networkConfiguration.creationDate")}",
                        "environmentsConfiguration": [
                            {
                                "name": "AGENT_ENV",
                                "targets": [
                                    {
                                        "name": "fake_target",
                                        "url": "http://fake_url:1234/fake"
                                    }
                                ]
                            }
                        ],
                        "agentNetworkConfiguration": ${#jsonSerialize(#jsonPath(#networkConfiguration, "$.networkConfiguration.agentNetworkConfiguration"))}
                    },
                    "agentsGraph": ${#jsonSerialize(#jsonPath(#networkConfiguration, "$.agentsGraph"))}
                }
                """
                Validate httpStatusCode_200 ${#status == 200}
        Then target FAKE_TARGET is saved locally
            Request environment conf from Chutney instance
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v2/environment
                    Take environments ${#body}
                    Validate httpStatusCode_200 ${#status == 200}
            Do compare Check target is present
                With actual ${#json(#environments, "$[0].targets[0].name")}
                With expected fake_target
                With mode equals
