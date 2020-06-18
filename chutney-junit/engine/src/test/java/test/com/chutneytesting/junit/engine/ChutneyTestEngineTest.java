package test.com.chutneytesting.junit.engine;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;

import com.chutneytesting.junit.engine.ChutneyTestEngine;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

class ChutneyTestEngineTest {

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
            .debug(System.out)
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
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
    void should_not_select_and_execute_filtered_with_include_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectClasspathResource("features/simple/success.feature"))
            .filters(includePackageNames("features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_exclude_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectClasspathResource("features/simple/success.feature"))
            .filters(excludePackageNames("features.simple"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_select_and_execute_classpath_root() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(singleton(Paths.get("target/test-classes")));
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(10).finished(10).succeeded(9).failed(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_include_classpath_root() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(singleton(Paths.get("target/test-classes")));
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .filters(includePackageNames("features.other"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_exclude_classpath_root() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(singleton(Paths.get("target/test-classes")));
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .filters(excludePackageNames("features.simple", "features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
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
    void should_not_select_and_execute_filtered_with_include_package_files() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectPackage("features.specific"))
            .filters(includePackageNames("features.simple"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_exclude_package_files() {
        EngineExecutionResults result = EngineTestKit.engine("chutney-junit-engine")
            .selectors(selectPackage("features.specific"))
            .filters(excludePackageNames("features"))
            .execute();

        result
            .allEvents()
            .debug(System.out)
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    @Disabled
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
