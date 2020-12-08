# language: en
@Edition
Feature: Support previous test case format

    Scenario Outline: Save or Update a test case (<scenario>) using raw edition with an old format
        When saving a test case with a raw <scenario> written in an old format
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2/raw
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Saved scenario",
                    "description":"contains a raw scenario in old format",
                    "content":"${#escapeJson(#resourceContent('raw_scenarios/<scenario>', null))}"
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the persisted test case is converted to the last format
            Do sql Request scenario version from Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT CONTENT_VERSION FROM SCENARIO WHERE ID = '${#scenarioId}' and ACTIVATED = TRUE |
                Take scenarioVersion ${#recordResult.get(0).rows.get(0).get(0)}
            Do compare Assert scenario version
                With actual ${#scenarioVersion}
                With expected v2.1
                With mode equals

        Examples:
            | scenario                          |
            | scenario.v0.json                  |
            | scenario.v0.hjson                 |
            | scenario.v1.json                  |
            | scenario.v1.hjson                 |
            | raw_scenario_json_task.v2.0.json  |
            | raw_scenario_hjson_task.v2.0.json |

    Scenario Outline: Consult an existing old format scenario (<scenario>) using GWT form view
        Given an existing <scenario> written in format <content_version>
            Do sql Insert scenario in Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT nextval('SCENARIO_SEQ') |
                Take scenarioId ${#recordResult.get(0).rows.get(0).get(0)}
            Do sql
                On CHUTNEY_DB
                With statements
                | INSERT INTO SCENARIO (CONTENT_VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, UPDATE_DATE, VERSION) VALUES ('<content_version>', ${#scenarioId}, 'already existing scenario', null, '${#escapeSql(#str_replace(#resourceContent("raw_scenarios/<scenario>", null), "\\R", " "))}', null, CURRENT_TIMESTAMP(6), '{}', TRUE, CURRENT_TIMESTAMP(6), 1) |
            Do compare Assert sql insert success
                With actual ${T(Long).toString(#recordResult.get(0).affectedRows)}
                With expected 1
                With mode equals
        When it is retrieved
            Do http-get Request scenario from Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2/${#scenarioId}
                Take retrievedScenario ${#body}
        Then it is viewed in the new format
            Do compare Check Given keyword
                With actual ${#json(#retrievedScenario, "$.scenario.givens").size().toString()}
                With expected <givens>
                With mode equals
            Do compare Check When keyword
                With actual ${#json(#retrievedScenario, "$.scenario.when.sentence")}
                With expected <when_sentence>
                With mode equals
            Do compare Check Then keyword
                With actual ${#json(#retrievedScenario, "$.scenario.thens").size().toString()}
                With expected <thens>
                With mode equals
        But still persisted in its original format <content_version>
            Do sql Request scenario version from Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT CONTENT_VERSION FROM SCENARIO WHERE ID = '${#scenarioId}' and ACTIVATED = TRUE |
                Take scenarioVersion ${#recordResult.get(0).rows.get(0).get(0)}
            Do compare Assert scenario version
                With actual ${#scenarioVersion}
                With expected <content_version>
                With mode equals

        Examples:
            | scenario                          | content_version | givens | when_sentence | thens |
            | scenario.v0.json                  | v0.0            | 3      |               | 1     |
            | scenario.v0.hjson                 | v0.0            | 3      |               | 1     |
            | scenario.v1.json                  | v1.0            | 1      | debug name    | 1     |
            | scenario.v1.hjson                 | v1.0            | 1      | debug name    | 1     |
            | raw_scenario_json_task.v2.0.json  | v2.0            | 1      |               | 0     |
            | raw_scenario_hjson_task.v2.0.json | v2.0            | 1      |               | 0     |

    Scenario Outline: Consult an existing scenario (<scenario>) using RAW view
        Given an existing <scenario> written in format <content_version>
            Do sql Insert scenario in Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT nextval('SCENARIO_SEQ') |
                Take scenarioId ${#recordResult.get(0).rows.get(0).get(0)}
            Do sql
                On CHUTNEY_DB
                With statements
                | INSERT INTO SCENARIO (CONTENT_VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, UPDATE_DATE, VERSION) VALUES ('<content_version>', ${#scenarioId}, 'already existing scenario', null, '${#escapeSql(#str_replace(#resourceContent("raw_scenarios/<scenario>", null), "\\R", " "))}', null, CURRENT_TIMESTAMP(6), '{}', TRUE, CURRENT_TIMESTAMP(6), 1) |
            Do compare Assert sql insert success
                With actual ${T(Long).toString(#recordResult.get(0).affectedRows)}
                With expected 1
                With mode equals
        When it is retrieved as raw
            Do http-get Request scenario from Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2/raw/${#scenarioId}
                Take retrievedRawScenario ${#body}
        Then it is viewed as raw
            Do compare Check for raw content
                With actual ${#json(#retrievedRawScenario, "$.content").isEmpty().toString()}
                With expected false
                With mode equals

        Examples:
            | scenario                          | content_version |
            | scenario.v0.json                  | v0.0            |
            | scenario.v0.hjson                 | v0.0            |
            | scenario.v1.json                  | v1.0            |
            | scenario.v1.hjson                 | v1.0            |
            | raw_scenario_json_task.v2.0.json  | v2.0            |
            | raw_scenario_hjson_task.v2.0.json | v2.0            |

    Scenario Outline: Execute an existing scenario (<scenario>)
        Given an existing <scenario> written in format <content_version>
            Do sql Insert scenario in Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT nextval('SCENARIO_SEQ') |
                Take scenarioId ${#recordResult.get(0).rows.get(0).get(0)}
            Do sql
                On CHUTNEY_DB
                With statements
                | INSERT INTO SCENARIO (CONTENT_VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, UPDATE_DATE, VERSION) VALUES ('<content_version>', ${#scenarioId}, 'already existing scenario', null, '${#escapeSql(#str_replace(#resourceContent("raw_scenarios/<scenario>", null), "\\R", " "))}', null, CURRENT_TIMESTAMP(6), '{}', TRUE, CURRENT_TIMESTAMP(6), 1) |
            Do compare Assert sql insert success
                With actual ${T(Long).toString(#recordResult.get(0).affectedRows)}
                With expected 1
                With mode equals
        And an execution environment
            Do http-post Create environment
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "EDITION_ENV_<env_id>_OK",
                    "description": "",
                    "targets": []
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/EDITION_ENV_<env_id>_OK
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
            | scenario                                 | content_version | env_id |
            | scenario.v0.json                         | v0.0            | 1      |
            | scenario.v0.hjson                        | v0.0            | 2      |
            | scenario.v1.json                         | v1.0            | 3      |
            | scenario.v1.hjson                        | v1.0            | 4      |
            | scenario_executable_json_task.v2.0.json  | v2.0            | 5      |
            | scenario_executable_hjson_task.v2.0.json | v2.0            | 6      |
            | scenario_executable.v2.1.json            | v2.1            | 7      |

    Scenario Outline: Execute existing scenario (<scenario>) containing errors
        Given an existing <scenario> written in format <content_version>
            Do sql Insert scenario in Chutney database
                On CHUTNEY_DB
                With statements
                | SELECT nextval('SCENARIO_SEQ') |
                Take scenarioId ${#recordResult.get(0).rows.get(0).get(0)}
            Do sql
                On CHUTNEY_DB
                With statements
                | INSERT INTO SCENARIO (CONTENT_VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, UPDATE_DATE, VERSION) VALUES ('<content_version>', ${#scenarioId}, 'already existing scenario', null, '${#escapeSql(#str_replace(#resourceContent("raw_scenarios/<scenario>", null), "\\R", " "))}', null, CURRENT_TIMESTAMP(6), '{}', TRUE, CURRENT_TIMESTAMP(6), 1) |
            Do compare Assert sql insert success
                With actual ${T(Long).toString(#recordResult.get(0).affectedRows)}
                With expected 1
                With mode equals
        And an execution environment
            Do http-post Create environment
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "EDITION_ENV_<env_id>_KO",
                    "description": "",
                    "targets": []
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario execution produces an error
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/EDITION_ENV_<env_id>_KO
                Take report ${#body}
            Do compare Assert HTTP status is 500
                With actual ${T(Integer).toString(#status)}
                With expected 500
                With mode equals
        Then the error has the message <message>
            Do compare Check error message
                With actual ${#report}
                With expected <message>
                With mode contains

        Examples:
            | content_version | scenario                          | message                                                                                                                | env_id |
            | v0.0            | scenario_unknown_target.v0.hjson  | com.chutneytesting.environment.domain.TargetNotFoundException: Target [UNKNOWN_TARGET] not found in environment | 1      |

    Scenario Outline: Task implementation (<scenario>) is always HJSON readable
        When saving a test case with a <scenario> written with GWT form
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Saved scenario",
                    "description":"contains a gwt scenario",
                    "scenario": ${#resourceContent("raw_scenarios/<scenario>", null)}
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the task implementation is HJSON readable
            retrieved as raw
                Do http-get Request scenario from Chutney instance
                    On CHUTNEY_LOCAL
                    With uri /api/scenario/v2/raw/${#scenarioId}
                    Take retrievedRawScenario ${#body}
                Do compare
                    With actual ${#str_replace(#json(#retrievedRawScenario, "$.content"), "\s+", "")}
                    With expected {type:fake_typetarget:FAKE_TARGETinputs:{fake_param:fake_value}}
                    With mode contains
            retrieved as GWT
                Do http-get Request scenario from Chutney instance
                    On CHUTNEY_LOCAL
                    With uri /api/scenario/v2/${#scenarioId}
                    Take retrievedScenario ${#body}
                Do compare
                    With actual ${#str_replace(#json(#retrievedScenario, "$.scenario.givens[0].implementation.task"), "\s+", "")}
                    With expected {type:fake_typetarget:FAKE_TARGETinputs:{fake_param:fake_value}}
                    With mode equals

        Examples:
            | scenario                      |
            | scenario_json_task.v2.0.json  |
            | scenario_hjson_task.v2.0.json |
            | scenario.v2.1.json            |
