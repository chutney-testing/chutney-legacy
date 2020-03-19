# language: en

Feature: SQL feature
    Could execute sql task as glacio feature

    Scenario: Simple sql select
        When Execute (sql) Request RDS BDD
            On RDS <url>
            | driverClassName       | oracle.jdbc.OracleDriver |
            | jdbcUrl               | jdbc:oracle:thin:@(DESCRIPTION_LIST=(FAILOVER=ON)(LOAD_BALANCE=OFF)(DESCRIPTION=(RETRY_COUNT=3)(ADDRESS=(PROTOCOL=TCP)(HOST=<host>)(PORT=1531))(CONNECT_DATA=(SERVICE_NAME=<service>)))) |
            | dataSource.user       | <user> |
            | dataSource.password   | <pass> |
            | maximumPoolSize       | 5 |
            With statements
            | SELECT * from <table> |
            | SELECT * from <table> |
            firstResult ${#recordResult.get(0)}
            secondResult ${#recordResult.get(1)}
        Then Execute debug
