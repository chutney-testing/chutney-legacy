# language: en
@SQL
Feature: SQL Task test

Scenario: Sql query success
    Given a target test with url tcp://localhost:12345 with properties
    | jdbcUrl | jdbc:h2:mem:fake-test;DB_CLOSE_DELAY=-1|
    | username | sa |
    | password | <empty> |
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        title: sql query success,
        givens: [
            {
              description: Create users table
              implementation:
              {
                type: sql
                target: test
                inputs:
                {
                  statements:
                  [
                    CREATE TABLE users ( id INTEGER PRIMARY KEY, name VARCHAR(30), email VARCHAR(50) )
                  ]
                }
              }
            }
        ]
      when:
      {
        description: Insert users
        implementation:
        {
          type: sql
          target: test
          inputs:
          {
            statements:
            [
              INSERT INTO users VALUES (1, 'laitue', 'laitue@fake.com')
              INSERT INTO users VALUES (2, 'carotte', 'kakarot@fake.db')
            ]
          }
        }
      }
      thens:
      [
        {
          description: Select all users
          implementation:
          {
            type: sql
            target: test
            inputs:
            {
              statements:
              [
                SELECT * FROM users
              ]
            }
          }
        }
      ]
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is SUCCESS
    And the last record results is ["Output: (recordResult) : ([Records{affectedRows=-1, headers=[ID, NAME, EMAIL], rows=[[1, laitue, laitue@fake.com], [2, carotte, kakarot@fake.db]]}])"]

Scenario: Sql query wrong table
    Given a target test with url tcp://localhost:12345 with properties
    | jdbcUrl | jdbc:h2:mem:fake-test;DB_CLOSE_DELAY=-1 |
    | username | sa |
    | password | <empty> |
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "sql query success",
            "steps": [
                {
                   "name": "Select all records from non existing table",
                    "type": "sql",
                    "target": "test",
                    "inputs": {
                        "statements": [
                            "SELECT * FROM toto"
                        ]
                    }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE
