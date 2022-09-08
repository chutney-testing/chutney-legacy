# language: en
@SSH
Feature: SSH Task test

    Scenario: Scenario execution unable to login, status SUCCESS and command stderr
        Given an SSHD server is started
            Do ssh-server-start
                With usernames
                | user |
                Wtih passwords
                | pass |
        And Target containing SSHD connection information with wrong password
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "SSH_ENV_KO",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_ssh",
                            "url": "ssh://${#sshServer.host()}:${#sshServer.port()}",
                            "properties": [
                                { "key" : "username", "value": "user" },
                                { "key" : "password", "value": "wrongpass" }
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
                    "title":"ssh action scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Execute commands",
                            "implementation":{
                                "action":"{\n type: ssh-client \n target: test_ssh \n inputs: {\n commands: [\n echo test \n] \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/SSH_ENV_KO
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals

    Scenario: Scenario execution with multiple ssh action
        Given an SSHD server is started
            Do ssh-server-start
        And Target containing SSHD connection information
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "SSH_ENV_OK",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_ssh",
                            "url": "ssh://${#sshServer.host()}:${#sshServer.port()}",
                            "properties": [
                                { "key" : "username", "value": "test" },
                                { "key" : "password", "value": "test" }
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
                    "title":"ssh action scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Execute commands",
                            "implementation":{
                                "action":"{\n type: ssh-client \n target: test_ssh \n inputs: {\n commands: [\n {\n command: echo test \n timeout: 500 ms \n},{\n command: echo testbis \n} \n] \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Assert results",
                                "subSteps":[
                                    {
                                        "sentence": "Assert first command",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(0).command.command} \n expected: echo test \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert first command timeout",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(0).command.timeout.toString()} \n expected: 500 ms \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert first command exit code",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${T(Integer).toString(#results.get(0).exitCode)} \n expected: \"0\" \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert first command stdout",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(0).stdout} \n expected: \"\" \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert first command sterr",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(0).stderr} \n expected: \"\" \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert second command",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(1).command.command} \n expected: echo testbis \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert second command timeout",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(1).command.timeout.toString()} \n expected: 5000 ms \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert second command exit code",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${T(Integer).toString(#results.get(1).exitCode)} \n expected: \"0\" \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert second command stdout",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(1).stdout} \n expected: \"\" \n mode: equals \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Assert second command sterr",
                                        "implementation":{
                                            "action":"{\n type: compare \n inputs: {\n actual: \${#results.get(1).stderr} \n expected: \"\" \n mode: equals \n} \n}"
                                        }
                                    }
                                ]
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/SSH_ENV_OK
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
        And the SSHD server has received the commands
            Do compare Check first command
                With actual ${#sshServer.command(0)}
                With expected echo test
                With mode equals
            Do compare Check second command
                With actual ${#sshServer.command(1)}
                With expected echo testbis
                With mode equals
