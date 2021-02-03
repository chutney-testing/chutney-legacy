package com.chutneytesting.design.infra.storage.scenario.git;

import static com.chutneytesting.tools.Try.unsafe;
import static java.util.Optional.empty;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.infra.storage.scenario.DelegateScenarioRepository;
import com.chutneytesting.design.infra.storage.scenario.git.json.versionned.JsonMapper;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.chutneytesting.tools.IoUtils;
import com.chutneytesting.tools.Streams;
import com.chutneytesting.tools.ThrowingRunnable;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitScenarioRepository implements DelegateScenarioRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitScenarioRepository.class);

    private final GitRepository gitRepository;

    private final Path localPath;
    private final GitClient gitClient;
    private final JsonMapper<TestCaseData> jsonMapper;

    public GitScenarioRepository(GitRepository gitRepository,
                                 GitClient gitClient,
                                 JsonMapper<TestCaseData> jsonMapper) {

        this.gitRepository = gitRepository;
        this.gitClient = gitClient;
        this.jsonMapper = jsonMapper;
        this.localPath = gitClient.getGitDirectory(gitRepository.repositoryName).resolve(gitRepository.testSubFolder);
        gitClient.loadRepository(gitRepository.url, gitRepository.repositoryName);
    }

    @Override
    public String alias() {
        return gitRepository.repositoryName;
    }

    @Override
    public String save(TestCaseData testCaseData) {
        return unsafe("Cannot save scenario", () -> {
            Path toWrite = prepareFile(testCaseData);
            Files.write(toWrite, jsonMapper.write(testCaseData).getBytes());

            final String commitMessage = "Auto commit/push. File committed: /" + testCaseData.title;
            gitClient.addCommitPushFile(gitRepository.repositoryName, commitMessage);

            return String.valueOf(testCaseData.title.hashCode());
        });
    }

    private Path prepareFile(TestCaseData testCaseData) throws IOException {
        Optional<Path> potentialExistingFile = findFile(testCaseData.id);

        Path toWrite = potentialExistingFile
            .map(path -> path.getParent().resolve(testCaseData.title))
            .orElse(localPath.resolve(testCaseData.title));

        potentialExistingFile.ifPresent(existingFile -> unsafe((ThrowingRunnable) () -> {
            if (shouldMoveFile(testCaseData.title, existingFile.getFileName().toString()))
                Files.move(existingFile, toWrite);
        }));

        if (!Files.exists(toWrite)) Files.createFile(toWrite);

        return toWrite;
    }

    @Override
    public Optional<TestCaseData> findById(String scenarioId) {
        return findFile(scenarioId).map(file -> unsafe(() -> read(file)));
    }

    private TestCaseData read(Path file) throws IOException {
        try (Reader reader = new FileReader(file.toFile())) {
            return jsonMapper.read(reader, json ->
                TestCaseDataMapper.mapFile(file));
        }
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return unsafe("Cannot browse " + localPath, () -> {
            gitClient.loadRepository(gitRepository.url, gitRepository.repositoryName);
            try (Stream<Path> paths = Files.walk(localPath)) {
                return paths
                    .filter(path -> path.toFile().isFile() && !IoUtils.isHidden(path, localPath))
                    .map(path -> ScenarioMetadataMapper.mapFile(path, alias()))
                    .collect(Collectors.toList());
            }
        });
    }

    @Override
    public void removeById(String scenarioId) {
        unsafe((ThrowingRunnable) () -> {
            final Optional<Path> path = findFile(scenarioId);
            if (path.isPresent()) {
                Files.delete(path.get());
                gitClient.removeCommitPushFile(gitRepository.repositoryName, "Delete file" + path.get(), path.get().toFile().getName());
            }
        });
    }

    @Override
    public Optional<Integer> lastVersion(String scenarioId) {
        return empty();
    }

    /**
     * @return path found if the filename hashcode equals to the scenarioId
     */
    private Optional<Path> findFile(String scenarioId) {
        try {
            try (Stream<Path> stream = Files.walk(localPath)) {
                return stream
                    .filter(path -> path.toFile().isFile() && String.valueOf(path.toFile().getName().hashCode()).equals(scenarioId))
                    .collect(Streams.collectUniqueResult());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot find file", e);
        }
    }

    private boolean shouldMoveFile(String title, String oldFileName) {
        return !oldFileName.equals(title);
    }

    private static class TestCaseDataMapper {
        static TestCaseData mapFile(Path path) {
            try {
                return TestCaseData.builder()
                    .withContentVersion("GIT")
                    .withId(String.valueOf(path.toFile().getName().hashCode()))
                    .withTitle(path.toFile().getName())
                    .withCreationDate(getCreationDate(path))
                    .withDescription("")
                    .withTags(Collections.emptyList())
                    .withExecutionParameters(Collections.emptyMap())
                    .withRawScenario(new String(Files.readAllBytes(path)))
                    .build();
            } catch (IOException e) {
                throw new RuntimeException("Cannot parse file " + path, e);
            }
        }
    }

    private static class ScenarioMetadataMapper {
        static TestCaseMetadata mapFile(Path path, String origin) {

            String id = String.valueOf(path.toFile().getName().hashCode());
            String title = path.toFile().getName();
            String description = "";
            return TestCaseMetadataImpl.builder()
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withCreationDate(getCreationDate(path))
                .withRepositorySource(origin)
                .build();
        }
    }

    private static Instant getCreationDate(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(path.toFile().toURI()), BasicFileAttributes.class);
            FileTime fileTime = attributes.creationTime();
            return fileTime.toInstant();
        } catch (IOException e) {
            LOGGER.error("Cannot parse file " + path, e);
            return Instant.MIN;
        }
    }
}
