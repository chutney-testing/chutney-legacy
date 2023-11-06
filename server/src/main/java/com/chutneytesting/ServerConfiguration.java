package com.chutneytesting;

import static com.chutneytesting.ServerConfigurationValues.CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.ENGINE_DELEGATION_PASSWORD_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.ENGINE_DELEGATION_USER_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.ENGINE_EXECUTOR_POOL_SIZE_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.ENGINE_REPORTER_PUBLISHER_TTL_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SERVER_PORT_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.TASK_SQL_NB_LOGGED_ROW;
import static com.chutneytesting.ServerConfigurationValues.TASK_SQL_NB_LOGGED_ROW_SPRING_VALUE;

import com.chutneytesting.action.api.EmbeddedActionEngine;
import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.campaign.domain.CampaignService;
import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.design.domain.editionlock.TestCaseEditions;
import com.chutneytesting.design.domain.editionlock.TestCaseEditionsService;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.execution.infra.execution.ExecutionRequestMapper;
import com.chutneytesting.execution.infra.execution.ServerTestEngineJavaImpl;
import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.scenario.infra.TestCaseRepositoryAggregator;
import com.chutneytesting.server.core.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngine;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngineAsync;
import com.chutneytesting.server.core.domain.execution.ServerTestEngine;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessor;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessors;
import com.chutneytesting.server.core.domain.execution.state.ExecutionStateRepository;
import com.chutneytesting.server.core.domain.instrument.ChutneyMetrics;
import com.chutneytesting.tools.ui.MyMixInForIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, ActiveMQAutoConfiguration.class, MongoAutoConfiguration.class})
public class ServerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

    @Value(SERVER_PORT_SPRING_VALUE)
    int port;

    @PostConstruct
    public void logPort() throws UnknownHostException {
        LOGGER.debug("Starting server " + InetAddress.getLocalHost().getCanonicalHostName() + " on " + port);
    }

    /**
     * For com.chutneytesting.ServerConfiguration#executionConfiguration()
     */
    @Bean
    public ThreadPoolTaskExecutor engineExecutor(@Value(ENGINE_EXECUTOR_POOL_SIZE_SPRING_VALUE) Integer threadForEngine) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadForEngine);
        executor.setMaxPoolSize(threadForEngine);
        executor.setThreadNamePrefix("engine-executor");
        executor.initialize();
        LOGGER.debug("Pool for engine created with size {}", threadForEngine);
        return executor;
    }

    /**
     * For com.chutneytesting.ServerConfiguration#campaignExecutionEngine()
     */
    @Bean
    public TaskExecutor campaignExecutor(@Value(CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE) Integer threadForCampaigns) {
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
        @Qualifier("engineExecutor") ThreadPoolTaskExecutor engineExecutor,
        @Value(TASK_SQL_NB_LOGGED_ROW_SPRING_VALUE) String nbLoggedRow,
        @Value(ENGINE_DELEGATION_USER_SPRING_VALUE) String delegateUser,
        @Value(ENGINE_DELEGATION_PASSWORD_SPRING_VALUE) String delegatePassword
    ) {
        Map<String, String> actionsConfiguration = new HashMap<>();
        actionsConfiguration.put(TASK_SQL_NB_LOGGED_ROW, nbLoggedRow);
        return new ExecutionConfiguration(reporterTTL, engineExecutor.getThreadPoolExecutor(), actionsConfiguration, delegateUser, delegatePassword);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:changelog/db.changelog-master.xml");
        liquibase.setContexts("!test");
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
                                                              @Qualifier("reportObjectMapper") ObjectMapper objectMapper,
                                                              @Value(EXECUTION_ASYNC_PUBLISHER_TTL_SPRING_VALUE) long replayerRetention,
                                                              @Value(EXECUTION_ASYNC_PUBLISHER_DEBOUNCE_SPRING_VALUE) long debounceMilliSeconds) {
        return new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            objectMapper,
            replayerRetention,
            debounceMilliSeconds);
    }

    @Bean
    TestCasePreProcessors testCasePreProcessors(List<TestCasePreProcessor> processors) {
        return new TestCasePreProcessors(processors);
    }

    @Bean
    CampaignExecutionEngine campaignExecutionEngine(CampaignRepository campaignRepository,
                                                    CampaignExecutionRepository campaignExecutionRepository,
                                                    ScenarioExecutionEngine scenarioExecutionEngine,
                                                    ExecutionHistoryRepository executionHistoryRepository,
                                                    TestCaseRepositoryAggregator testCaseRepository,
                                                    Optional<DataSetHistoryRepository> dataSetHistoryRepository,
                                                    JiraXrayEmbeddedApi jiraXrayEmbeddedApi,
                                                    ChutneyMetrics metrics,
                                                    @Qualifier("campaignExecutor") TaskExecutor campaignExecutor,
                                                    DataSetRepository datasetRepository,
                                                    ObjectMapper objectMapper) { // TODO - Choose explicitly which mapper to use
        return new CampaignExecutionEngine(
            campaignRepository,
            campaignExecutionRepository,
            scenarioExecutionEngine,
            executionHistoryRepository,
            testCaseRepository,
            dataSetHistoryRepository,
            jiraXrayEmbeddedApi,
            metrics,
            new ExecutorServiceAdapter(campaignExecutor),
            datasetRepository,
            objectMapper
        );
    }

    @Bean
    TestCaseEditionsService testCaseEditionsService(TestCaseEditions testCaseEditions, TestCaseRepositoryAggregator testCaseRepository) {
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
    EmbeddedActionEngine embeddedActionEngine(ExecutionConfiguration executionConfiguration) {
        return new EmbeddedActionEngine(executionConfiguration.actionTemplateRegistry());
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    CampaignService campaignService(CampaignRepository campaignRepository) {
        return new CampaignService(campaignRepository);
    }


    // TODO - To move in infra when it will not be used in domain (ScenarioExecutionEngineAsync)
    @Bean
    public ObjectMapper reportObjectMapper() {
        SimpleModule jdomElementModule = new SimpleModule();
        jdomElementModule.addSerializer(Element.class, new JDomElementSerializer());

        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .registerModule(jdomElementModule)
            .findAndRegisterModules();
    }

    // TODO - To remove when Reporter will serialize itself
    static class JDomElementSerializer extends StdSerializer<Element> {

        private static final long serialVersionUID = 1L;

        protected JDomElementSerializer() {
            this(null);
        }

        protected JDomElementSerializer(Class<Element> t) {
            super(t);
        }

        @Override
        public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String xmlString = new XMLOutputter(Format.getCompactFormat()).outputString(element);
            jsonGenerator.writeObject(xmlString);
        }
    }
}
