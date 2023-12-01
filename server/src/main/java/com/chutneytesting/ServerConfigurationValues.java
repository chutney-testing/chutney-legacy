/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting;

public final class ServerConfigurationValues {
    public static final String SERVER_PORT_SPRING_VALUE = "${server.port}";
    public static final String SERVER_INSTANCE_NAME_VALUE = "${server.instance-name:''}";
    public static final String SERVER_HTTP_PORT_SPRING_VALUE = "${server.http.port}";
    public static final String SERVER_HTTP_INTERFACE_SPRING_VALUE = "${server.http.interface}";
    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.configuration-folder:~/.chutney/conf}";
    public static final String ENGINE_REPORTER_PUBLISHER_TTL_SPRING_VALUE = "${chutney.engine.reporter.publisher.ttl:5}";
    public static final String ENGINE_DELEGATION_USER_SPRING_VALUE = "${chutney.engine.delegation.user:#{null}}";
    public static final String ENGINE_DELEGATION_PASSWORD_SPRING_VALUE = "${chutney.engine.delegation.password:#{null}}";
    public static final String EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE = "${chutney.server.execution.async.publisher.ttl:5}";
    public static final String EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE = "${chutney.server.execution.async.publisher.debounce:250}";
    public static final String CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.server.campaigns.executor.pool-size:20}";
    public static final String SCHEDULED_CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.server.schedule-campaigns.executor.pool-size:20}";
    public static final String SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE = "${chutney.server.schedule-campaigns.fixed-rate:60000}";
    public static final String SCHEDULED_PURGE_CRON_SPRING_VALUE = "${chutney.server.schedule-purge.cron:0 0 1 * * *}";
    public static final String SCHEDULED_PURGE_MAX_SCENARIO_EXECUTIONS_SPRING_VALUE = "${chutney.server.schedule-purge.max-scenario-executions:10}";
    public static final String SCHEDULED_PURGE_MAX_CAMPAIGN_EXECUTIONS_SPRING_VALUE = "${chutney.server.schedule-purge.max-campaign-executions:10}";
    public static final String ENGINE_EXECUTOR_POOL_SIZE_SPRING_VALUE = "${chutney.engine.executor.pool-size:20}";
    public static final String AGENT_NETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE = "${chutney.server.agent.network.connection-checker-timeout:1000}";
    public static final String LOCAL_AGENT_DEFAULT_NAME_SPRING_VALUE = "${chutney.server.agent.name:#{null}}";
    public static final String LOCAL_AGENT_DEFAULT_HOSTNAME_SPRING_VALUE = "${chutney.server.agent.hostname:#{null}}";
    public static final String EDITIONS_TTL_VALUE_SPRING_VALUE = "${chutney.server.editions.ttl.value:6}";
    public static final String EDITIONS_TTL_UNIT_SPRING_VALUE = "${chutney.server.editions.ttl.unit:HOURS}";
    public static final String TASK_SQL_NB_LOGGED_ROW = "chutney.actions.sql.max-logged-rows";
    public static final String TASK_SQL_NB_LOGGED_ROW_SPRING_VALUE = "${" + TASK_SQL_NB_LOGGED_ROW + ":30}";
}
