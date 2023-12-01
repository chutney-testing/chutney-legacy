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

package test.com.chutneytesting.junit.engine;

import static com.chutneytesting.junit.engine.ChutneyTestEngine.CHUTNEY_JUNIT_ENGINE_ID;
import static com.chutneytesting.junit.engine.DiscoverySelectorResolver.FEATURE_SEGMENT_TYPE;
import static com.chutneytesting.junit.engine.DiscoverySelectorResolver.SCENARIO_SEGMENT_TYPE;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
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
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

class ChutneyTestEngineTest {

    private final ChutneyTestEngine sut = new ChutneyTestEngine();

    @Test
    void should_get_engineId() {
        assertThat(sut.getId()).isEqualTo(CHUTNEY_JUNIT_ENGINE_ID);
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
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectFile("src/test/resources/features/simple/success.feature"))
            .execute();

        result
            .allEvents()
            .debug(System.out)
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

    @Test
    void should_select_and_execute_directory_files() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectDirectory("src/test/resources/features"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(10).finished(10).succeeded(9).failed(1));
    }

    @Test
    void should_select_and_execute_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectClasspathResource("features/simple/success.feature"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

    @Test
    void should_not_select_and_execute_filtered_with_include_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectClasspathResource("features/simple/success.feature"))
            .filters(includePackageNames("features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_exclude_classpath_resource() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
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
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(10).finished(10).succeeded(9).failed(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_include_classpath_root() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(singleton(Paths.get("target/test-classes")));
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
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
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(classpathRootSelectors.toArray(new ClasspathRootSelector[0]))
            .filters(excludePackageNames("features.simple", "features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_select_and_execute_package_files() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectPackage("features.specific"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(3).finished(3).succeeded(3));
    }

    @Test
    void should_not_select_and_execute_filtered_with_include_package_files() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectPackage("features.specific"))
            .filters(includePackageNames("features.simple"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_not_select_and_execute_filtered_with_exclude_package_files() {
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectPackage("features.specific"))
            .filters(excludePackageNames("features"))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(1).finished(1).succeeded(1));
    }

    @Test
    void should_select_and_execute_feature_uniqueId_with_classpathroot() {
        List<ClasspathRootSelector> classpathRootSelectors = selectClasspathRoots(singleton(Paths.get("target/test-classes")));
        UniqueId featureUniqueId = UniqueId.forEngine(CHUTNEY_JUNIT_ENGINE_ID)
            .append(FEATURE_SEGMENT_TYPE, "success.feature");

        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectUniqueId(featureUniqueId), classpathRootSelectors.get(0))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

    @Test
    void should_select_and_execute_scenario_uniqueId_without_classpathroot() {
        UniqueId scenarioUniqueId = UniqueId.forEngine(CHUTNEY_JUNIT_ENGINE_ID)
            .append(FEATURE_SEGMENT_TYPE, "success.feature")
            .append(SCENARIO_SEGMENT_TYPE, "Success feature - Substeps Success");

        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectUniqueId(scenarioUniqueId))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(3).finished(3).succeeded(3));
    }

    @Test
    @Disabled
    void should_select_and_execute_uri_jar_file() {
        String root = Paths.get("").toAbsolutePath().toUri().getSchemeSpecificPart();
        URI uri = URI.create("jar:file:" + root + "/src/test/resources/features.jar!/features/simple/success.feature");
        EngineExecutionResults result = EngineTestKit.engine(CHUTNEY_JUNIT_ENGINE_ID)
            .selectors(selectUri(uri))
            .execute();

        result
            .allEvents()
            .assertStatistics(stats -> stats.started(4).finished(4).succeeded(4));
    }

}
