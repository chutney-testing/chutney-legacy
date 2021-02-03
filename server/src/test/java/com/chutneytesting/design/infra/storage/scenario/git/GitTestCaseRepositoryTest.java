package com.chutneytesting.design.infra.storage.scenario.git;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.infra.storage.scenario.git.json.versionned.JsonMapper;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GitTestCaseRepositoryTest {

    Path testPath;
    Path file1;
    Path file2;

    String testDirectory = "testDir";
    String scenario1Id = String.valueOf("testFile1.json".hashCode());
    String scenario2Id = String.valueOf("testFile2.json".hashCode());
    private JsonMapper<TestCaseData> jsonMapper;

    @BeforeEach
    public void setUp() throws Exception {
        testPath = Paths.get(System.getProperty("user.home") + "/" + testDirectory);
        file1 = testPath.resolve("testFile1.json");
        file2 = testPath.resolve("subdir/testFile2.json");

        cleanTestDirectory();

        Files.createDirectory(testPath);
        Files.createDirectory(testPath.resolve("subdir"));
        Files.createFile(file1);
        Files.createFile(file2);
    }

    @Test
    public void should_create_and_update_scenario() throws Exception {
        String scenarioName = "newScenario.json";
        Path shouldCreatedFile = testPath.resolve(scenarioName);

        try {
            GitScenarioRepository gitScenarioRepository = createTestGitRepository();
            TestCaseData scenario = TestCaseData.builder()
                .withContentVersion("GIT")
                .withId("0")
                .withTitle(scenarioName)
                .withCreationDate(Instant.now())
                .withDescription("")
                .withTags(Collections.emptyList())
                .withExecutionParameters(Collections.emptyMap())
                .withRawScenario("pouet")
                .build();

            when(jsonMapper.write(same(scenario))).thenReturn(scenario.rawScenario);

            String idGenerated = gitScenarioRepository.save(scenario);

            assertThat(idGenerated).isEqualTo(String.valueOf(scenarioName.hashCode()));
            assertThat(shouldCreatedFile).exists();
            assertThat(shouldCreatedFile).hasContent("pouet");

            TestCaseData scenarioToUpdate = TestCaseData.builder()
                .withContentVersion("GIT")
                .withId(idGenerated)
                .withTitle(scenarioName)
                .withDescription("")
                .withTags(Collections.emptyList())
                .withExecutionParameters(Collections.emptyMap())
                .withRawScenario("%#pas_pouet#%")
                .build();

            when(jsonMapper.write(same(scenarioToUpdate))).thenReturn(scenarioToUpdate.rawScenario);

            String idUpdated = gitScenarioRepository.save(scenarioToUpdate);

            assertThat(idUpdated).isEqualTo(String.valueOf(scenarioName.hashCode()));
            assertThat(shouldCreatedFile).exists();
            assertThat(shouldCreatedFile).hasContent("%#pas_pouet#%");

        } finally {
            // clean
            Files.deleteIfExists(shouldCreatedFile);
        }
    }

    @Test
    public void should_rename_scenario() {
        GitScenarioRepository gitScenarioRepository = createTestGitRepository();
        TestCaseData.TestCaseDataBuilder scenarioToMoveBuilder = TestCaseData.builder()
            .withContentVersion("GIT")
            .withId(scenario2Id)
            .withTitle("other_name.json")
            .withDescription("")
            .withTags(Collections.emptyList())
            .withExecutionParameters(Collections.emptyMap())
            .withRawScenario("12345");

        TestCaseData scenarioToMove = scenarioToMoveBuilder.build();

        when(jsonMapper.write(same(scenarioToMove))).thenReturn("some content");

        String newId = gitScenarioRepository.save(scenarioToMove);

        assertThat(testPath.resolve("subdir/other_name.json")).exists();
        assertThat(newId).isEqualTo(String.valueOf("other_name.json".hashCode()));

        // move again
        scenarioToMove = scenarioToMoveBuilder.withContentVersion("GIT").withId(newId).withTitle("testFile2.json").build();
        when(jsonMapper.write(same(scenarioToMove))).thenReturn("some content");
        String move2 = gitScenarioRepository.save(scenarioToMove);

        assertThat(testPath.resolve("subdir/testFile2.json")).exists();
        assertThat(move2).isEqualTo(scenario2Id);
    }

    @Test
    public void should_find_scenario_by_id() {
        GitScenarioRepository gitScenarioRepository = createTestGitRepository();
        when(jsonMapper.read(any(), any())).thenReturn(TestCaseData.builder().withContentVersion("GIT").withId("0").build());

        Optional<TestCaseData> scenario = gitScenarioRepository.findById(scenario1Id);

        assertThat(scenario).isPresent();
    }

    @Test
    public void should_return_all_scenarios() {
        GitScenarioRepository gitScenarioRepository = createTestGitRepository();

        final List<TestCaseMetadata> all = gitScenarioRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TestCaseMetadata::creationDate).isNotNull();
        assertThat(all).extracting(TestCaseMetadata::creationDate).isNotEqualTo(Instant.MIN);
    }

    @Test
    public void should_delete_scenario() throws Exception {
        GitScenarioRepository gitScenarioRepository = createTestGitRepository();

        gitScenarioRepository.removeById(scenario1Id);

        Optional<TestCaseData> scenario = gitScenarioRepository.findById(scenario1Id);

        assertThat(scenario).isEmpty();

        // clean
        Files.createFile(file1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanTestDirectory();
    }

    private void cleanTestDirectory() throws IOException {
        if (testPath.toFile().exists()) {
            try (Stream<Path> stream = Files.walk(testPath)) {
                stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private GitScenarioRepository createTestGitRepository() {
        GitRepository conf = new GitRepository(1L, "", testDirectory, "test");
        GitClient gitClient = mock(GitClient.class, RETURNS_DEEP_STUBS);
        when(gitClient.getGitDirectory(any())).thenReturn(Paths.get(System.getProperty("user.home")));
        jsonMapper = mock(JsonMapper.class);
        return new GitScenarioRepository(conf, gitClient, jsonMapper);
    }

}
