# language: en
@TestCaseEditionMetadata
Feature: Support testcase edition metadata

    Scenario: Consult new testcase metadata
        Given robert has created a testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        When admin consult it
        Then the title is titre
        And the description is a testcase
        And the tags is first,second
        And the creation date is 2020-01-01T12:00:03Z
        And the author is robert
        And the update date is equal to creation date
        And the version is equal to 1

    Scenario: Consult new composable testcase metadata
        Given robert has created a composable testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        When admin consult it
        Then the title is titre
        And the description is a testcase
        And the tags is first,second
        And the creation date is 2020-01-01T12:00:03Z
        And the author is robert
        And the update date is set by the system
        And the version is equal to 1

    Scenario: Consult testcase metadata after update
        Given robert has created a testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        And paloma has updated it with metadata
            | title     | description     | tags         | author    | creationDate         | updateDate           | version |
            | new Title | new description | second,third | notEditor | 2020-06-01T14:00:00Z | 2001-01-01T00:00:00Z | 1       |
        When admin consult it
        Then the title is new Title
        And the description is new description
        And the tags is second,third
        And the creation date is 2020-06-01T14:00:00Z
        And the author is paloma
        And the update date is set by the system
        And the version is equal to 2

    Scenario: Consult composable testcase metadata after update
        Given robert has created a composable testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        And paloma has updated it with metadata
            | title     | description     | tags         | author    | creationDate         | updateDate           | version |
            | new Title | new description | second,third | notEditor | 2020-06-01T14:00:00Z | 2001-01-01T00:00:00Z | 1       |
        When admin consult it
        Then the title is new Title
        And the description is new description
        And the tags is second,third
        And the creation date is 2020-01-01T12:00:03Z
        And the author is paloma
        And the update date is set by the system
        And the version is equal to 2

    Scenario: Update testcase with wrong version
        Given robert has created a testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        When paloma updates it with wrong version
            | title     | description     | tags         | author    | creationDate         | updateDate           | version |
            | new Title | new description | second,third | notEditor | 2020-06-01T14:00:00Z | 2001-01-01T00:00:00Z | 666     |
        Then request fails with error status 404 and message contains version [666] not found

    Scenario: Update composable testcase with wrong version
        Given robert has created a composable testcase with metadata
            | title | description | tags         | author     | creationDate         | updateDate           | version |
            | titre | a testcase  | first,second | notCreator | 2020-01-01T12:00:03Z | 2020-02-02T12:00:03Z | 111     |
        When paloma updates it with wrong version
            | title     | description     | tags         | author    | creationDate         | updateDate           | version |
            | new Title | new description | second,third | notEditor | 2020-06-01T14:00:00Z | 2001-01-01T00:00:00Z | 999     |
        Then request fails with error status 404 and message contains version [999] not found
