# language: en
@DataSet
Feature: Dataset management

    Background:
        Given a dataset is saved
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

    Scenario: Find existing dataset
        When search for the dataset
        Then the dataset is retrieved

    Scenario: Delete exisiting dataset
        When delete the dataset
        Then the dataset cannot be found

    Scenario: Versionning
        When a new version is saved
        """
        {
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
        Then the dataset last version number is 2
        And the list of version numbers is
        | 1 | 2 |
        And the search for the dataset bring the last version
        And the dataset version 1 can be found
