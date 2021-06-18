# language: en
@SQL
Feature: SQL Task test

    Scenario: Sql query success
        Given a sql database target
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "SQL_ENV_OK",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_sql",
                            "url": "tcp://localhost:12345",
                            "properties": [
                                { "key": "jdbcUrl", "value": "jdbc:h2:mem:fake-test;DB_CLOSE_DELAY=-1" },
                                { "key": "username", "value": "sa" },
                                { "key": "password", "value": "" }
                            ]
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
                    "title":"sql query success",
                    "scenario":{
                        "givens":[
                            {
                                "sentence":"Create users table",
                                "implementation":{
                                    "task":"{\n type: sql \n target: test_sql \n inputs: {\n statements: [\n CREATE TABLE users ( id INTEGER PRIMARY KEY, name VARCHAR(30), email VARCHAR(50) ) \n] \n}\n}"
                                }
                            }
                        ],
                        "when":{
                            "sentence":"Insert users",
                            "implementation":{
                                "task":"{\n type: sql \n target: test_sql \n inputs: {\n statements: [\n INSERT INTO users VALUES (1, 'laitue', 'laitue@fake.com') \n INSERT INTO users VALUES (2, 'carotte', 'kakarot@fake.db') \n] \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Select all users",
                                "implementation":{
                                    "task":"{\n type: sql \n target: test_sql \n inputs: {\n statements: [\n SELECT * FROM users \n] \n} \n}"
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/SQL_ENV_OK
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
        And the report contains record results
            Do compare
                With actual ${#json(#report, "$.report.steps[-1:].stepOutputs.recordResult").toString()}
                With expected [[{"affectedRows":-1,"headers":["ID","NAME","EMAIL"],"rows":[[1,"laitue","laitue@fake.com"],[2,"carotte","kakarot@fake.db"]],"columns":[{"name":"ID","index":0},{"name":"NAME","index":1},{"name":"EMAIL","index":2}],"records":[{},{}]}]]
                With mode equals

    Scenario: Sql query wrong table
        Given a sql database target
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "SQL_ENV_KO",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_sql",
                            "url": "tcp://localhost:12345",
                            "properties": [
                                { "key": "jdbcUrl", "value": "jdbc:h2:mem:fake-test;DB_CLOSE_DELAY=-1" },
                                { "key": "username", "value": "sa" },
                                { "key": "password", "value": "" }
                            ]
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
                    "title":"sql query failure",
                    "scenario":{
                        "when":{
                            "sentence":"select unknown table",
                            "implementation":{
                                "task":"{\n type: sql \n target: test_sql \n inputs: {\n statements: [\n SELECT * FROM unknownTable \n] \n} \n}"
                            }
                        },
                        "thens":[]
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/SQL_ENV_KO
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
