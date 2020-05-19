package com.chutneytesting.cli;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.cli.infrastruture.ExecutionRequestMapper;
import com.chutneytesting.cli.infrastruture.ImmutableEnvironment;
import com.chutneytesting.cli.infrastruture.ImmutableScenarioContent;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.hjson.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main class.
 */
@Command(name = "chutney")
public class LiteEngineBootstrapRx implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiteEngineBootstrapRx.class);
    private static final ObjectMapper objectMapper = objectMapper();

    @Parameters(arity = "1..*", paramLabel = "FILE", description = "Scenario(s) to process.")
    private File[] scenarios;

    @Option(names = {"-env", "--env"}, description = "Path to environment file")
    private File environmentFile;

    @Override
    public void run() {
        ExecutionConfiguration executionConfiguration = new ExecutionConfiguration();

        ImmutableEnvironment originalEnvironmentObject = getEnvironment(environmentFile);
        final TestEngine testEngine = executionConfiguration.embeddedTestEngine();

        Observable.fromArray(scenarios)
            .map(this::getScenario)
            .map(scenario ->  ExecutionRequestMapper.toDto(scenario, originalEnvironmentObject))
            .map(testEngine::executeAsync)
            .concatMap(executionId -> {
                Observable<StepExecutionReportDto> reports = testEngine.receiveNotification(executionId);
                reports.subscribeOn(Schedulers.io())
                    .subscribe(report -> { System.out.println(report.name + " - " + report.status ); });
                return reports;
            })
            .blockingSubscribe( a->{}, b -> {}, () -> LOGGER.info("All scenarios executed"));
    }

    private ImmutableScenarioContent getScenario(File filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath.toPath());
            String jsonContent = JsonValue.readHjson(new String(bytes)).toString();
            return objectMapper.readValue(jsonContent, ImmutableScenarioContent.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandLine.ParameterException(new CommandLine(this), "Cannot read file at path [" + filePath.toPath() + "]");
        }
    }

    private ImmutableEnvironment getEnvironment(File environmentFile) {
        ImmutableEnvironment originalEnvironmentObject;
        try {
            byte[] bytes = Files.readAllBytes(environmentFile.toPath());
            originalEnvironmentObject = objectMapper.readValue(new String(bytes), ImmutableEnvironment.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load environment file", e);
        }
        return originalEnvironmentObject;
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }

    public static void main(String... args) {
        CommandLine.run(new LiteEngineBootstrapRx(), args);
    }
}
