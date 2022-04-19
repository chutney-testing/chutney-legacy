# language: en
@Edition
Feature: Support previous test case format

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
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/EDITION_ENV_<env_id>_OK
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

        Examples:
            | scenario_executable.v2.1.json            | v2.1            | 7      |

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
                Validate httpStatusCode_200 ${#status == 200}
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
