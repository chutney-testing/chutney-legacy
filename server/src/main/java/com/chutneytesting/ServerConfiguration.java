package com.chutneytesting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.EnvironmentService;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessor;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngineAsync;
import com.chutneytesting.execution.domain.scenario.ServerTestEngine;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.state.ExecutionStateRepository;
import com.chutneytesting.execution.infra.execution.ExecutionRequestMapper;
import com.chutneytesting.execution.infra.execution.ServerTestEngineJavaImpl;
import com.chutneytesting.instrument.domain.Metrics;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, ActiveMQAutoConfiguration.class, MongoAutoConfiguration.class})
@EnableScheduling
@EnableAsync
public class ServerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

    @Value("${server.port}")
    int port;

    @PostConstruct
    public void logPort() throws UnknownHostException {
        LOGGER.debug("Starting server " + InetAddress.getLocalHost().getCanonicalHostName() + " on " + port);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:changelog/db.changelog-master.xml");
        liquibase.setDataSource(dataSource);
        return liquibase;
    }

    @Bean
    LocalServerIdentifier localServerIdentifier(@Value("${server.port}") int port,
                                                @Value("${localAgent.defaultName:#{null}}") Optional<String> defaultLocalName,
                                                @Value("${localAgent.defaultHostName:#{null}}") Optional<String> defaultLocalHostName
    ) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        return new LocalServerIdentifier(
            port,
            defaultLocalName.orElse(localHost.getHostName()),
            defaultLocalHostName.orElse(localHost.getCanonicalHostName()));
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
                                                              Metrics metrics,
                                                              TestCasePreProcessors testCasePreProcessors,
                                                              ObjectMapper objectMapper,
                                                              @Value("${chutney.execution.async.publisher.ttl:5}") long replayerRetention,
                                                              @Value("${chutney.execution.async.publisher.debounce:250}") long debounceMilliSeconds) {
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
                                                    ScenarioExecutionEngine scenarioExecutionEngine,
                                                    ExecutionHistoryRepository executionHistoryRepository,
                                                    TestCaseRepository testCaseRepository) {
        return new CampaignExecutionEngine(campaignRepository, scenarioExecutionEngine, executionHistoryRepository, testCaseRepository);
    }

    @Bean
    EnvironmentService environmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    @Bean
    ServerTestEngine javaTestEngine(@Qualifier("embeddedTestEngine") TestEngine testEngine,
                                    ExecutionRequestMapper executionRequestMapper) {
        return new ServerTestEngineJavaImpl(testEngine, executionRequestMapper);
    }

}
