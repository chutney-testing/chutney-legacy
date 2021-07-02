# language: en

Feature: Chutney api security

    Scenario Outline: A user must be authenticated and has <authority> authority to access <uri> with verb <http-verb>
        Given Current roles and authorizations
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/authorizations
                Take currentAuthorizations ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        And Current network configuration
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/description
                Take networkConfiguration ${#body}
        When An unknown user access the api
            Do http-<http-verb>
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                        |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("unknown:user").getBytes())} |
                With uri <uri>
        Then The HTTP response status is 401
            Do compare
                With actual ${#status}
                With expected 401
                With mode equals
        When A known user access the api without the correct authority then the HTTP response status is 403 (softly:)
            Check user has no authority
                Do http-get
                    On CHUTNEY_LOCAL_NO_USER
                    With headers
                    | Content-Type  | application/json;charset=UTF-8                                                             |
                    | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                    With uri /api/v1/user
                Do compare
                    With actual ${#status}
                    With expected 200
                    With mode equals
                Do json-assert
                    With document ${#body}
                    With expected
                    | $.authorizations | $isEmpty |
            Do http-<http-verb> Access the api
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                             |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                With uri <uri>
                With body <body>
            Check the HTTP response status is 403
                Do compare
                    With actual ${#status}
                    With expected 403
                    With mode equals
            Remove the authority to the user (clean)
                Do http-post Post roles and authorizations to Chutney instance
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    With headers
                    | Content-Type | application/json;charset=UTF-8 |
                    With body ${#jsonPath(#currentAuthorizations, "$")}
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
        When A known user access the api with the correct <authority> authority then the HTTP response status is <status> (softly:)
            Add the authority to the user
                Do context-put
                    With entries
                    | roleName | <authority>_ROLE |
                    Take roleAuthorizations ${#jsonPath('{"name": "'+#roleName+'", "users":["secutest"]}', "$")}
                Do http-post Post roles and authorizations to Chutney instance
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    With headers
                    | Content-Type | application/json;charset=UTF-8 |
                    With body
                    """
                    {
                        "roles": ${#jsonPath(#currentAuthorizations, '$.roles')},
                        "authorizations": ${#jsonSerialize(#jsonPath(#currentAuthorizations, "$.authorizations[?(@.name!='"+#roleName+"')]").appendElement(#roleAuthorizations))}
                    }
                    """
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
            Check user authority is <authority>
                Do http-get
                    On CHUTNEY_LOCAL_NO_USER
                    With headers
                    | Content-Type  | application/json;charset=UTF-8                                                             |
                    | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                    With uri /api/v1/user
                Do compare
                    With actual ${#status}
                    With expected 200
                    With mode equals
                Do json-assert
                    With document ${#body}
                    With expected
                    | $.authorizations[0] | <authority> |
            Do http-<http-verb> Access the api
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                             |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                With uri <uri>
                With timeout 5 s
                With body <body>
            Check HTTP response status is <status>
                Do compare
                    With actual ${#status}
                    With expected <status>
                    With mode equals
        And Clean roles and authorizations
            Do http-post Post roles and authorizations to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/authorizations
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body ${#jsonPath(#currentAuthorizations, "$")}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals

        Examples:
            | http-verb | uri                                                                | authority          | body                                                                                   | status |
            | get       | /api/v1/backups/git                                                | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/v1/backups/git                                                | ADMIN_ACCESS       | {"name":"secuback","url":"","branch":"","privateKeyPath":"","privateKeyPassphrase":""} | 404    |
            | delete    | /api/v1/backups/git/name                                           | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /api/v1/backups/git/name/backup                                    | ADMIN_ACCESS       |                                                                                        | 404    |
            | get       | /api/v1/backups                                                    | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/v1/backups                                                    | ADMIN_ACCESS       | {"homePage":true}                                                                      | 200    |
            | get       | /api/v1/backups/backupId                                           | ADMIN_ACCESS       |                                                                                        | 404    |
            | delete    | /api/v1/backups/backupId                                           | ADMIN_ACCESS       |                                                                                        | 404    |
            | get       | /api/v1/backups/id/download                                        | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/v1/admin/database/execute/orient                              | ADMIN_ACCESS       | select 1                                                                               | 200    |
            | post      | /api/v1/admin/database/execute/jdbc                                | ADMIN_ACCESS       | select 1                                                                               | 200    |
            | post      | /api/v1/admin/database/paginate/orient                             | ADMIN_ACCESS       | {"pageNumber":1,"elementPerPage":1,"wrappedRequest":""}                                | 200    |
            | post      | /api/v1/admin/database/paginate/jdbc                               | ADMIN_ACCESS       | {"pageNumber":1,"elementPerPage":1,"wrappedRequest":""}                                | 200    |
            | get       | /api/source/git/v1                                                 | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/source/git/v1                                                 | ADMIN_ACCESS       | {}                                                                                     | 200    |
            | delete    | /api/source/git/v1/666                                             | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/homepage/v1                                                   | ADMIN_ACCESS       | {"content":""}                                                                         | 200    |
            | post      | /api/v1/agentnetwork/configuration                                 | ADMIN_ACCESS       | \${#jsonPath(#networkConfiguration, "\$.networkConfiguration")}                        | 200    |
            | get       | /api/v1/description                                                | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/v1/agentnetwork/explore                                       | ADMIN_ACCESS       | {"creationDate":"1235"}                                                                | 200    |
            | post      | /api/v1/agentnetwork/wrapup                                        | ADMIN_ACCESS       | \${#networkConfiguration}                                                              | 200    |
            | post      | /api/ui/campaign/v1                                                | CAMPAIGN_WRITE     | {"title":"secu","scenarioIds":[],"tags":[]}                                            | 200    |
            | put       | /api/ui/campaign/v1                                                | CAMPAIGN_WRITE     | {"title":"secu","scenarioIds":[],"tags":[]}                                            | 200    |
            | delete    | /api/ui/campaign/v1/666                                            | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | get       | /api/ui/campaign/v1/666                                            | CAMPAIGN_READ      |                                                                                        | 404    |
            | get       | /api/ui/campaign/v1/666/scenarios                                  | CAMPAIGN_READ      |                                                                                        | 200    |
            | get       | /api/ui/campaign/v1                                                | CAMPAIGN_READ      |                                                                                        | 200    |
            | get       | /api/ui/campaign/v1/lastexecutions/20                              | CAMPAIGN_READ      |                                                                                        | 200    |
            | get       | /api/ui/campaign/v1/scenario/scenarioId                            | SCENARIO_READ      |                                                                                        | 200    |
            | get       | /api/ui/campaign/v1/scheduling                                     | CAMPAIGN_READ      |                                                                                        | 200    |
            | post      | /api/ui/campaign/v1/scheduling                                     | CAMPAIGN_WRITE     | {}                                                                                     | 200    |
            | delete    | /api/ui/campaign/v1/scheduling/666                                 | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | get       | /api/v1/datasets                                                   | DATASET_READ       |                                                                                        | 200    |
            | get       | /api/v1/datasets                                                   | SCENARIO_WRITE     |                                                                                        | 200    |
            | get       | /api/v1/datasets                                                   | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | post      | /api/v1/datasets                                                   | DATASET_WRITE      | {"name":"secu1"}                                                                       | 200    |
            | put       | /api/v1/datasets                                                   | DATASET_WRITE      | {"name":"secu2"}                                                                       | 404    |
            | delete    | /api/v1/datasets/dataSetId                                         | DATASET_WRITE      |                                                                                        | 404    |
            | get       | /api/v1/datasets/dataSetId                                         | DATASET_READ       |                                                                                        | 404    |
            | get       | /api/v1/datasets/dataSetId/versions/last                           | DATASET_READ       |                                                                                        | 404    |
            | get       | /api/v1/datasets/dataSetId/versions                                | DATASET_READ       |                                                                                        | 200    |
            | get       | /api/v1/datasets/dataSetId/versions/666                            | DATASET_READ       |                                                                                        | 404    |
            | get       | /api/v1/datasets/dataSetId/666                                     | DATASET_READ       |                                                                                        | 404    |
            | get       | /api/v1/editions/testcases/testcaseId                              | SCENARIO_READ      |                                                                                        | 200    |
            | post      | /api/v1/editions/testcases/testcaseId                              | SCENARIO_WRITE     | {}                                                                                     | 404    |
            | delete    | /api/v1/editions/testcases/testcaseId                              | SCENARIO_WRITE     |                                                                                        | 200    |
            | get       | /api/ui/globalvar/v1                                               | GLOBAL_VAR_READ    |                                                                                        | 200    |
            | post      | /api/ui/globalvar/v1/secupost                                      | GLOBAL_VAR_WRITE   | {"message":"{}"}                                                                       | 200    |
            | delete    | /api/ui/globalvar/v1/secudelete                                    | GLOBAL_VAR_WRITE   |                                                                                        | 404    |
            | get       | /api/ui/globalvar/v1/secuget                                       | GLOBAL_VAR_READ    |                                                                                        | 404    |
            | get       | /api/ui/jira/v1/scenario                                           | SCENARIO_READ      |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/scenario                                           | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/campaign                                           | CAMPAIGN_READ      |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/scenario/scenarioId                                | SCENARIO_WRITE     |                                                                                        | 200    |
            | post      | /api/ui/jira/v1/scenario                                           | SCENARIO_WRITE     | {"id":"","chutneyId":""}                                                               | 200    |
            | delete    | /api/ui/jira/v1/scenario/scenarioId                                | SCENARIO_WRITE     |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/campaign/campaignId                                | CAMPAIGN_READ      |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/testexec/testExecId                                | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | post      | /api/ui/jira/v1/campaign                                           | CAMPAIGN_WRITE     | {"id":"","chutneyId":""}                                                               | 200    |
            | delete    | /api/ui/jira/v1/campaign/campaignId                                | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/configuration                                      | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/configuration/url                                  | SCENARIO_READ      |                                                                                        | 200    |
            | get       | /api/ui/jira/v1/configuration/url                                  | CAMPAIGN_READ      |                                                                                        | 200    |
            | post      | /api/ui/jira/v1/configuration                                      | ADMIN_ACCESS       | {"url":"","username":"","password":""}                                                 | 200    |
            | post      | /api/v1/ui/plugins/linkifier/                                      | ADMIN_ACCESS       | {"pattern":"","link":"","id":""}                                                       | 200    |
            | delete    | /api/v1/ui/plugins/linkifier/id                                    | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/scenario/component-edition                                    | SCENARIO_WRITE     | {"title":"","scenario":{}}                                                             | 200    |
            | get       | /api/scenario/component-edition/testCaseId                         | SCENARIO_READ      |                                                                                        | 404    |
            | delete    | /api/scenario/component-edition/testCaseId                         | SCENARIO_WRITE     |                                                                                        | 200    |
            | get       | /api/scenario/component-edition/testCaseId/executable              | SCENARIO_READ      |                                                                                        | 404    |
            | get       | /api/scenario/component-edition/testCaseId/executable/parameters   | CAMPAIGN_WRITE     |                                                                                        | 404    |
            | post      | /api/steps/v1                                                      | COMPONENT_WRITE    | {"name":""}                                                                            | 200    |
            | delete    | /api/steps/v1/stepId                                               | COMPONENT_WRITE    |                                                                                        | 200    |
            | get       | /api/steps/v1/all                                                  | COMPONENT_READ     |                                                                                        | 200    |
            | get       | /api/steps/v1/all                                                  | SCENARIO_WRITE     |                                                                                        | 200    |
            | get       | /api/steps/v1/stepId/parents                                       | COMPONENT_READ     |                                                                                        | 404    |
            | get       | /api/steps/v1                                                      | COMPONENT_READ     |                                                                                        | 200    |
            | get       | /api/steps/v1/stepId                                               | COMPONENT_READ     |                                                                                        | 404    |
            | get       | /api/scenario/v2/testCaseId                                        | SCENARIO_READ      |                                                                                        | 404    |
            | get       | /api/scenario/v2                                                   | SCENARIO_READ      |                                                                                        | 200    |
            | get       | /api/scenario/v2                                                   | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | post      | /api/scenario/v2                                                   | SCENARIO_WRITE     | {"title":"","scenario":{"when":{}}}                                                    | 200    |
            | patch     | /api/scenario/v2                                                   | SCENARIO_WRITE     | {"title":"","scenario":{"when":{}}}                                                    | 200    |
            | delete    | /api/scenario/v2/testCaseId                                        | SCENARIO_WRITE     |                                                                                        | 200    |
            | post      | /api/scenario/v2/raw                                               | SCENARIO_WRITE     | {"title":"","content":""}                                                              | 200    |
            | get       | /api/scenario/v2/raw/testCaseId                                    | SCENARIO_READ      |                                                                                        | 404    |
            | get       | /api/documentation                                                 | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/documentation                                                 | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /api/ui/campaign/execution/v1/campaignName                         | CAMPAIGN_EXECUTE   |                                                                                        | 200    |
            | get       | /api/ui/campaign/execution/v1/campaignName/env                     | CAMPAIGN_EXECUTE   |                                                                                        | 200    |
            | post      | /api/ui/campaign/execution/v1/replay/666                           | CAMPAIGN_EXECUTE   | {}                                                                                     | 404    |
            | get       | /api/ui/campaign/execution/v1/campaignPattern/surefire             | CAMPAIGN_EXECUTE   |                                                                                        | 200    |
            | get       | /api/ui/campaign/execution/v1/campaignPattern/surefire/env         | CAMPAIGN_EXECUTE   |                                                                                        | 200    |
            | post      | /api/ui/campaign/execution/v1/666/stop                             | CAMPAIGN_EXECUTE   | {}                                                                                     | 404    |
            | get       | /api/ui/campaign/execution/v1/byID/666                             | CAMPAIGN_EXECUTE   |                                                                                        | 404    |
            | get       | /api/ui/campaign/execution/v1/byID/666/env                         | CAMPAIGN_EXECUTE   |                                                                                        | 404    |
            | get       | /api/ui/scenario/scenarioId/execution/v1                           | SCENARIO_READ      |                                                                                        | 200    |
            | get       | /api/ui/scenario/scenarioId/execution/666/v1                       | SCENARIO_READ      |                                                                                        | 404    |
            | post      | /api/ui/scenario/execution/v1/scenarioId/env                       | SCENARIO_EXECUTE   |                                                                                        | 404    |
            | post      | /api/ui/component/execution/v1/componentId/env                     | COMPONENT_WRITE    |                                                                                        | 404    |
            | post      | /api/idea/scenario/execution/env                                   | SCENARIO_EXECUTE   | {"content":"{}","params":{}}                                                           | 200    |
            | post      | /api/ui/scenario/executionasync/v1/scenarioId/env                  | SCENARIO_EXECUTE   | []                                                                                     | 404    |
            | get       | /api/ui/scenario/executionasync/v1/scenarioId/execution/666        | SCENARIO_READ      |                                                                                        | 404    |
            | post      | /api/ui/scenario/executionasync/v1/scenarioId/execution/666/stop   | SCENARIO_EXECUTE   |                                                                                        | 404    |
            | post      | /api/ui/scenario/executionasync/v1/scenarioId/execution/666/pause  | SCENARIO_EXECUTE   |                                                                                        | 404    |
            | post      | /api/ui/scenario/executionasync/v1/scenarioId/execution/666/resume | SCENARIO_EXECUTE   |                                                                                        | 404    |
            | post      | /api/v1/authorizations                                             | ADMIN_ACCESS       | {}                                                                                     | 200    |
            | get       | /api/v1/authorizations                                             | ADMIN_ACCESS       |                                                                                        | 200    |
            | post      | /api/scenario/execution/v1                                         | SCENARIO_EXECUTE   | {"scenario":{}}                                                                        | 200    |
            | get       | /api/task/v1                                                       | COMPONENT_READ     |                                                                                        | 200    |
            | get       | /api/task/v1/taskId                                                | COMPONENT_READ     |                                                                                        | 404    |
            | get       | /api/v2/environment                                                | ENVIRONMENT_ACCESS |                                                                                        | 200    |
            | get       | /api/v2/environment/names                                          | SCENARIO_EXECUTE   |                                                                                        | 200    |
            | get       | /api/v2/environment/names                                          | CAMPAIGN_WRITE     |                                                                                        | 200    |
            | get       | /api/v2/environment/names                                          | CAMPAIGN_EXECUTE   |                                                                                        | 200    |
            | get       | /api/v2/environment/names                                          | COMPONENT_WRITE    |                                                                                        | 200    |
            | post      | /api/v2/environment                                                | ENVIRONMENT_ACCESS | {"name": "secuenv"}                                                                    | 200    |
            | delete    | /api/v2/environment/envName                                        | ENVIRONMENT_ACCESS |                                                                                        | 404    |
            | put       | /api/v2/environment/envName                                        | ENVIRONMENT_ACCESS | {}                                                                                     | 404    |
            | get       | /api/v2/environment/envName/target                                 | ENVIRONMENT_ACCESS |                                                                                        | 404    |
            | get       | /api/v2/environment/target                                         | ENVIRONMENT_ACCESS |                                                                                        | 200    |
            | get       | /api/v2/environment/target/names                                   | COMPONENT_READ     |                                                                                        | 200    |
            | get       | /api/v2/environment/envName                                        | ENVIRONMENT_ACCESS |                                                                                        | 404    |
            | get       | /api/v2/environment/envName/target/targetName                      | ENVIRONMENT_ACCESS |                                                                                        | 404    |
            | post      | /api/v2/environment/envName/target                                 | ENVIRONMENT_ACCESS | {"name":"","url":""}                                                                   | 404    |
            | delete    | /api/v2/environment/envName/target/targetName                      | ENVIRONMENT_ACCESS |                                                                                        | 404    |
            | put       | /api/v2/environment/envName/target/targetName                      | ENVIRONMENT_ACCESS | {"name":"","url":""}                                                                   | 404    |
            | get       | /actuator                                                          | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/beans                                                    | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/caches                                                   | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/health                                                   | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/info                                                     | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/conditions                                               | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/configprops                                              | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/env                                                      | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/liquidbase                                               | ADMIN_ACCESS       |                                                                                        | 404    |
            | get       | /actuator/loggers                                                  | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/heapdump                                                 | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/threaddump                                               | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/prometheus                                               | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/metrics                                                  | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/scheduledtasks                                           | ADMIN_ACCESS       |                                                                                        | 200    |
            | get       | /actuator/mappings                                                 | ADMIN_ACCESS       |                                                                                        | 200    |

    Scenario Outline: A user must be authenticated without any authority to access <uri> with verb <http-verb>
        When An unknown user access the api
            Do http-<http-verb>
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                        |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("unknown:user").getBytes())} |
                With uri <uri>
        Then The HTTP response status is 401
            Do compare
                With actual ${#status}
                With expected 401
                With mode equals
        When A known user access the api without any authority
            Check user has no authority
                Do http-get
                    On CHUTNEY_LOCAL_NO_USER
                    With headers
                    | Content-Type  | application/json;charset=UTF-8                                                             |
                    | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                    With uri /api/v1/user
                Do compare
                    With actual ${#status}
                    With expected 200
                    With mode equals
                Do json-assert
                    With document ${#body}
                    With expected
                    | $.authorizations | $isEmpty |
            Do http-<http-verb> Access the api
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                             |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("secutest:secutest").getBytes())} |
                With uri <uri>
                With body
        Then The HTTP response status is 200
            Do compare
                With actual ${#status}
                With expected 200
                With mode equals

        Examples:
            | http-verb | uri                             |
            | get       | /api/v1/user                    |
            | post      | /api/v1/user                    |
            | get       | /api/v1/ui/plugins/linkifier/   |
            | get       | /api/homepage/v1                |
            | get       | /home                           |
