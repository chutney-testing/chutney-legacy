package com.chutneytesting.cli;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Strings;
import com.chutneytesting.cli.infrastruture.ExecutionRequestMapper;
import com.chutneytesting.cli.infrastruture.ImmutableScenarioContent;
import com.chutneytesting.cli.infrastruture.ScenarioContent;
import com.chutneytesting.ExecutionSpringConfiguration;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.hjson.JsonValue;
import com.chutneytesting.cli.infrastruture.ImmutableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main class.
 */
@Command(name = "chutney")
public class LiteEngineBootstrap implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiteEngineBootstrap.class);
    private static final ObjectMapper objectMapper = objectMapper();

    @Parameters(arity = "1..*", paramLabel = "FILE", description = "Scenario(s) to process.")
    private File[] scenarios;

    @Option(names = {"-env", "--env"}, description = "Path to environment file")
    private File environmentFile;

    @Override
    public void run() {
        final ApplicationContext context = startSpringContext();

        ImmutableEnvironment originalEnvironmentObject = getEnvironment(environmentFile);

        final TestEngine testEngine = (TestEngine) context.getBean("embeddedTestEngine");

        Arrays.asList(scenarios).stream().forEach(filePath -> {
            ScenarioContent scenario = getScenario(filePath);
            ExecutionRequestDto requestDto = ExecutionRequestMapper.toDto(scenario, originalEnvironmentObject);
            StepExecutionReportDto result = testEngine.execute(requestDto);
            printStep(result, 0);
        });
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

    private void printStep(StepExecutionReportDto s, int indent) {
        LOGGER.info(Strings.repeat(" ", indent * 5) + "Step " + s.name + " is " + s.status + " in " + s.duration + " ms");
        if (!s.errors.isEmpty()) {
            s.errors.stream().forEach(LOGGER::error);
        }
        s.steps.stream().forEach(sub -> printStep(sub, indent + 1));
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

    private ConfigurableApplicationContext startSpringContext() {
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ExecutionSpringConfiguration.class)
            .registerShutdownHook(true)
            .web(WebApplicationType.NONE)
            .bannerMode(Mode.OFF);

        return appBuilder.build().run();
    }

    public static void main(String... args) {
        CommandLine.run(new LiteEngineBootstrap(), args);
    }
}
