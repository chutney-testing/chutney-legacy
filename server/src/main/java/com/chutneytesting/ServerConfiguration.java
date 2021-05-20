package com.chutneytesting;

import static com.chutneytesting.task.sql.SqlTask.CONFIGURABLE_NB_LOGGED_ROW;

import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.editionlock.TestCaseEditions;
import com.chutneytesting.design.domain.editionlock.TestCaseEditionsService;
import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessor;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.jira.JiraXrayPlugin;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngineAsync;
import com.chutneytesting.execution.domain.scenario.ServerTestEngine;
import com.chutneytesting.execution.domain.state.ExecutionStateRepository;
import com.chutneytesting.execution.infra.execution.ExecutionRequestMapper;
import com.chutneytesting.execution.infra.execution.ServerTestEngineJavaImpl;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, ActiveMQAutoConfiguration.class, MongoAutoConfiguration.class})
@EnableScheduling
@EnableAsync
public class ServerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

    public static final String SERVER_PORT_SPRING_VALUE = "${server.port}";
    public static final String SERVER_HTTP_PORT_SPRING_VALUE = "${server.http.port}";
    public static final String SERVER_HTTP_INTERFACE_SPRING_VALUE = "${server.http.interface}";

    public static final String DBSERVER_PORT_SPRING_VALUE = "${chutney.db-server.port}";
    private static final String DBSERVER_BASEDIR_SPRING_BASE_VALUE = "${chutney.db-server.base-dir:~/.chutney/data";
    public static final String DBSERVER_H2_BASEDIR_SPRING_VALUE = DBSERVER_BASEDIR_SPRING_BASE_VALUE + "}";
    public static final String DBSERVER_PG_BASEDIR_SPRING_BASE_VALUE = DBSERVER_BASEDIR_SPRING_BASE_VALUE + "/pgdata}";
    public static final String DBSERVER_PG_WORKDIR_SPRING_BASE_VALUE = DBSERVER_BASEDIR_SPRING_BASE_VALUE + "/pgwork}";

    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.configuration-folder:~/.chutney/conf}";
    public static final String ENGINE_REPORTER_PUBLISHER_TTL_SPRING_VALUE = "${chutney.engine.reporter.publisher.ttl:5}";
    public static final String ENGINE_DELEGATION_USER_SPRING_VALUE = "${chutney.engine.delegation.user:#{null}}";
    public static final String ENGINE_DELEGATION_PASSWORD_SPRING_VALUE = "${chutney.engine.delegation.password:#{null}}";
    public static final String EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE = "${chutney.execution.async.publisher.ttl:5}";
    public static final String EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE = "${chutney.execution.async.publisher.debounce:250}";
    public static final String CAMPAIGNS_THREAD_SPRING_VALUE = "${chutney.campaigns.thread:20}";
    public static final String ENGINE_THREAD_SPRING_VALUE = "${chutney.scenarios.thread:20}";
    public static final String AGENTNETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE = "${chutney.agentnetwork.connection-checker-timeout:1000}";
    public static final String LOCALAGENT_DEFAULTNAME_SPRING_VALUE = "${chutney.localAgent.defaultName:#{null}}";
    public static final String LOCALAGENT_DEFAULTHOSTNAME_SPRING_VALUE = "${chutney.localAgent.defaultHostName:#{null}}";
    public static final String EXAMPLES_ACTIVE_SPRING_VALUE = "${chutney.examples.active:false}";
    public static final String EDITIONS_TTL_VALUE_SPRING_VALUE = "${chutney.editions.ttl.value:6}";
    public static final String EDITIONS_TTL_UNIT_SPRING_VALUE = "${chutney.editions.ttl.unit:HOURS}";

    public static final String TASK_SQL_NB_LOGGED_ROW = "${" + CONFIGURABLE_NB_LOGGED_ROW + ":30}";

    @Value(SERVER_PORT_SPRING_VALUE)
    int port;

    @PostConstruct
    public void logPort() throws UnknownHostException {
        LOGGER.debug("Starting server " + InetAddress.getLocalHost().getCanonicalHostName() + " on " + port);
    }

    @Bean
    public TaskExecutor engineExecutor(@Value(ENGINE_THREAD_SPRING_VALUE) Integer threadForEngine) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadForEngine);
        executor.setMaxPoolSize(threadForEngine);

        executor.setThreadNamePrefix("engine-executor");
        executor.initialize();
        LOGGER.debug("Pool for engine created with size {}", threadForEngine);
        return executor;
    }

    @Bean
    public TaskExecutor campaignExecutor(@Value(CAMPAIGNS_THREAD_SPRING_VALUE) Integer threadForCampaigns) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadForCampaigns);
        executor.setMaxPoolSize(threadForCampaigns);
        executor.setThreadNamePrefix("campaign-executor");
        executor.initialize();
        LOGGER.debug("Pool for campaigns created with size {}", threadForCampaigns);
        return executor;
    }

    @Bean
    public ExecutionConfiguration executionConfiguration(
        @Value(ENGINE_REPORTER_PUBLISHER_TTL_SPRING_VALUE) Long reporterTTL,
        Executor engineExecutor,
        @Value(TASK_SQL_NB_LOGGED_ROW) String nbLoggedRow,
        @Value(ENGINE_DELEGATION_USER_SPRING_VALUE) String delegateUser,
        @Value(ENGINE_DELEGATION_PASSWORD_SPRING_VALUE) String delegatePasword
    ) {
        Map<String, String> tasksConfiguration = new HashMap<>();
        tasksConfiguration.put(CONFIGURABLE_NB_LOGGED_ROW, nbLoggedRow);
        return new ExecutionConfiguration(reporterTTL, engineExecutor, tasksConfiguration, delegateUser, delegatePasword);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:changelog/db.changelog-master.xml");
        liquibase.setDataSource(dataSource);
        return liquibase;
    }

    @Bean
    ScenarioExecutionEngine scenarioExecutionEngine(ServerTestEngine executionEngine,
                                                    TestCasePreProcessors testCasePreProcessors,
                                                    ScenarioExecutionEngineAsync executionEngineAsync) {
        return new ScenarioExecutionEngine(
            executionEngine,
            testCasePreProcessors,
            executionEngineAsync);
    }

    @Bean
    ScenarioExecutionEngineAsync scenarioExecutionEngineAsync(ExecutionHistoryRepository executionHistoryRepository,
                                                              ServerTestEngine executionEngine,
                                                              ExecutionStateRepository executionStateRepository,
                                                              ChutneyMetrics metrics,
                                                              TestCasePreProcessors testCasePreProcessors,
                                                              ObjectMapper objectMapper,
                                                              DataSetHistoryRepository dataSetHistoryRepository,
                                                              @Value(EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE) long replayerRetention,
                                                              @Value(EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE) long debounceMilliSeconds) {
        return new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            objectMapper,
            dataSetHistoryRepository,
            replayerRetention,
            debounceMilliSeconds);
    }

    @Bean
    TestCasePreProcessors testCasePreProcessors(List<TestCasePreProcessor> processors) {
        return new TestCasePreProcessors(processors);
    }

    @Bean
    CampaignExecutionEngine campaignExecutionEngine(CampaignRepository campaignRepository,
                                                    ScenarioExecutionEngine scenarioExecutionEngine,
                                                    ExecutionHistoryRepository executionHistoryRepository,
                                                    TestCaseRepository testCaseRepository,
                                                    DataSetHistoryRepository dataSetHistoryRepository,
                                                    JiraXrayPlugin jiraXrayPlugin,
                                                    ChutneyMetrics metrics,
                                                    TaskExecutor campaignExecutor) {
        return new CampaignExecutionEngine(
            campaignRepository,
            scenarioExecutionEngine,
            executionHistoryRepository,
            testCaseRepository,
            dataSetHistoryRepository,
            jiraXrayPlugin,
            metrics,
            new ExecutorServiceAdapter(campaignExecutor)
        );
    }

    @Bean
    TestCaseEditionsService testCaseEditionsService(TestCaseEditions testCaseEditions, TestCaseRepository testCaseRepository) {
        return new TestCaseEditionsService(testCaseEditions, testCaseRepository);
    }

    @Bean
    TestEngine embeddedTestEngine(ExecutionConfiguration executionConfiguration) {
        return executionConfiguration.embeddedTestEngine();
    }

    @Bean
    ServerTestEngine javaTestEngine(TestEngine embeddedTestEngine, ExecutionRequestMapper executionRequestMapper) {
        return new ServerTestEngineJavaImpl(embeddedTestEngine, executionRequestMapper);
    }

    @Bean
    EmbeddedTaskEngine embeddedTaskEngine(ExecutionConfiguration executionConfiguration) {
        return new EmbeddedTaskEngine(executionConfiguration.taskTemplateRegistry());
    }

    @Bean
    JiraXrayPlugin jiraXrayPlugin(JiraRepository jiraRepository, ObjectMapper objectMapper) {
        return new JiraXrayPlugin(jiraRepository, objectMapper);
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
