# language: en
@DataSet
Feature: Dataset management

    Background:
        Given a dataset is saved
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "my dataset name",
                    "description": "my dataset description",
                    "lastUpdated": "2020-04-30T15:12:42.285Z",
                    "tags": [ "TAG1", "TAG2" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "uv1" }, { "key": "uk2", "value": "uv2" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": "mk2", "value": "mv21" }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv12" }, { "key": "mk2", "value": "mv22" }, { "key": "mk3", "value": "mv32" } ],
                        [ { "key": "mk1", "value": "mv13" }, { "key": "mk2", "value": "mv23" }, { "key": "mk3", "value": "mv33" } ]
                    ]
                }
                """
                Take datasetId ${#jsonPath(#body, "$.id")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals

    Scenario: Find existing dataset
        When search for the dataset
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
                Take dataset ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the dataset is retrieved
            Do compare Assert HTTP status is 200
                With actual ${#jsonPath(#dataset, "$.name")}
                With expected my dataset name
                With mode equals

    Scenario: Delete exisiting dataset
        When delete the dataset
            Do http-delete
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the dataset cannot be found
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
            Do compare Assert HTTP status is 404
                With actual ${T(Integer).toString(#status)}
                With expected 404
                With mode equals

    Scenario: Versionning
        When a new version is saved
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "id": "${#datasetId}",
                    "name": "new name",
                    "description": "new description",
                    "lastUpdated": "2020-05-01T10:09:00.134Z",
                    "tags": [ "NEW_TAG" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "new v1" }, { "key": "K3", "value": "uv3" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": "mk2", "value": "mv21" }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv122" }, { "key": "mk2", "value": "mv222" }, { "key": "mk3", "value": "mv322" } ]
                    ]
                }
                """
                Take datasetVersion ${#jsonPath(#body, "$.version")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the dataset last version number is 2
            Do compare
                With actual ${T(Integer).toString(#datasetVersion)}
                With expected 2
                With mode equals
        And the list of version numbers is [1,2]
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}/versions
                Take datasetVersionList ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
            Do compare Assert version's list
                With actual ${#jsonPath(#datasetVersionList, "$[*].version").toString()}
                With expected [1,2]
                With mode equals
        And the search for the dataset bring the last version
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
            Do compare Assert last version number
                With actual ${#jsonPath(#body, "$.version").toString()}
                With expected 2
                With mode equals
        And the dataset version 1 can be found
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}/1
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
            Do compare Assert version number
                With actual ${#jsonPath(#body, "$.version").toString()}
                With expected 1
                With mode equals
