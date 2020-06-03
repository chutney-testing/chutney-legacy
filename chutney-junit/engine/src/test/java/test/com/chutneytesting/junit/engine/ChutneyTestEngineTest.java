package test.com.chutneytesting.junit.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;

import com.chutneytesting.junit.engine.ChutneyTestEngine;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ChutneyTestEngineTest {

    private ChutneyTestEngine sut = new ChutneyTestEngine();

    @Test
    void should_get_engineId() {
        assertThat(sut.getId()).isEqualTo("chutney-junit-engine");
    }

    @Test
    void should_get_groupId() {
        assertThat(sut.getGroupId()).isEqualTo(Optional.of("com.chutneytesting"));
    }

    @Test
    void should_get_artifactId() {
        assertThat(sut.getArtifactId()).isEqualTo(Optional.of("chutney-junit-platform-engine"));
    }

    @Test
    void should_select_and_execute_file() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectFile("src/test/resources/features/simple/success.feature"))
            .execute();

        result
            .allEvents()
//            .debug(System.out)
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container("feature:success.feature"), started()),
                event(test("scenario:Success feature - Direct Success"), started()),
                event(test("scenario:Success feature - Direct Success"), finishedSuccessfully()),
                event(test("scenario:Success feature - Substeps Success"), started()),
                event(test("scenario:Success feature - Substeps Success"), finishedSuccessfully()),
                event(container("feature:success.feature"), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            );
    }

    @Test
    void should_select_and_execute_directory_files() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectDirectory("src/test/resources/features"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(10).finished(10).succeeded(9).failed(1));
    }

    @Test
    void should_select_and_execute_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectClasspathResource("features/simple/success.feature"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

    @Test
    void should_select_and_execute_classpath_root() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(Collections.singleton(Paths.get("src")));
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(10).finished(10).succeeded(9).failed(1));
    }

    @Test
    void should_select_and_execute_package_files() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectPackage("features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(3).finished(3).succeeded(3));
    }

    @Test
    void should_select_and_execute_uri_jar_file() {
        String root = Paths.get("").toAbsolutePath().toUri().getSchemeSpecificPart();
        URI uri = URI.create("jar:file:" + root + "/src/test/resources/features.jar!/features/simple/success.feature");
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectUri(uri))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

}
