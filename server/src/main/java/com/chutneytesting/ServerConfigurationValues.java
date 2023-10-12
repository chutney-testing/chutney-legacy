package com.chutneytesting;

public interface ServerConfigurationValues {
    String SERVER_PORT_SPRING_VALUE = "${server.port}";
    String SERVER_INSTANCE_NAME_VALUE = "${server.instance-name:''}";
    String SERVER_HTTP_PORT_SPRING_VALUE = "${server.http.port}";
    String SERVER_HTTP_INTERFACE_SPRING_VALUE = "${server.http.interface}";
    String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.configuration-folder:~/.chutney/conf}";
    String ENGINE_REPORTER_PUBLISHER_TTL_SPRING_VALUE = "${chutney.engine.reporter.publisher.ttl:5}";
    String ENGINE_DELEGATION_USER_SPRING_VALUE = "${chutney.engine.delegation.user:#{null}}";
    String ENGINE_DELEGATION_PASSWORD_SPRING_VALUE = "${chutney.engine.delegation.password:#{null}}";
    String EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE = "${chutney.server.execution.async.publisher.ttl:5}";
    String EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE = "${chutney.server.execution.async.publisher.debounce:250}";
    String CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.server.campaigns.executor.pool-size:20}";
    String SCHEDULED_CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.server.schedule-campaigns.executor.pool-size:20}";
    String SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE = "${chutney.server.schedule-campaigns.fixed-rate:60000}";
    String SCHEDULED_PURGE_CRON_SPRING_VALUE = "${chutney.server.schedule-purge.cron:0 0 1 * * *}";
    String SCHEDULED_PURGE_MAX_SCENARIO_EXECUTIONS_SPRING_VALUE = "${chutney.server.schedule-purge.max-scenario-executions:10}";
    String SCHEDULED_PURGE_MAX_CAMPAIGN_EXECUTIONS_SPRING_VALUE = "${chutney.server.schedule-purge.max-campaign-executions:10}";
    String ENGINE_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.engine.executor.pool-size:20}";
    String AGENT_NETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE = "${chutney.server.agent.network.connection-checker-timeout:1000}";
    String LOCAL_AGENT_DEFAULT_NAME_SPRING_VALUE = "${chutney.server.agent.name:#{null}}";
    String LOCAL_AGENT_DEFAULT_HOSTNAME_SPRING_VALUE = "${chutney.server.agent.hostname:#{null}}";
    String EDITIONS_TTL_VALUE_SPRING_VALUE = "${chutney.server.editions.ttl.value:6}";
    String EDITIONS_TTL_UNIT_SPRING_VALUE = "${chutney.server.editions.ttl.unit:HOURS}";
    String TASK_SQL_NB_LOGGED_ROW = "chutney.actions.sql.max-logged-rows";
    String TASK_SQL_NB_LOGGED_ROW_SPRING_VALUE = "${" + TASK_SQL_NB_LOGGED_ROW + ":30}";
}
