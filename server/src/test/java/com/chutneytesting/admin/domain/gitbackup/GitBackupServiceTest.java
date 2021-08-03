package com.chutneytesting.admin.domain.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.infra.gitbackup.GitClientImpl;
import com.chutneytesting.admin.infra.gitbackup.RemotesFileRepository;
import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitBackupServiceTest {

    private final Remotes remotesMock = mock(RemotesFileRepository.class);
    private final GitClient gitClientMock = mock(GitClientImpl.class);

    private final ChutneyContentProvider providerMock = new FakeProvider();
    private final Set<ChutneyContentProvider> contentProviders = Set.of(providerMock);

    @TempDir
    static Path temporaryFolder;

    @Test
    void should_remove_passphrase_when_asked_for_remotes() {
        RemoteRepository remote = new RemoteRepository("name", "u.r/l", "branch", "path/to/private/key", "passphrase");
        when(remotesMock.getAll()).thenReturn(singletonList(remote));

        GitBackupService sut = new GitBackupService(remotesMock, gitClientMock, contentProviders, temporaryFolder.toString());

        // When
        List<RemoteRepository> all = sut.getAll();

        // Then
        assertThat(all.get(0).privateKeyPassphrase).isNullOrEmpty();
    }

    @Test
    void should_throw_when_adding_a_remote_not_accessible() {
        // Given
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(gitClientMock.hasAccess(any())).thenReturn(false);

        GitBackupService sut = new GitBackupService(remotesMock, gitClientMock, contentProviders, temporaryFolder.toString());

        // When & Then
        assertThatThrownBy(() -> sut.add(remote))
            .isInstanceOf(UnreachableRemoteException.class);
    }

    @Test
    void should_add_when_adding_an_accessible_remote() {
        // Given
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(gitClientMock.hasAccess(any())).thenReturn(true);

        GitBackupService sut = new GitBackupService(remotesMock, gitClientMock, contentProviders, temporaryFolder.toString());

        // When
        sut.add(remote);

        // Then
        verify(remotesMock, times(1)).add(remote);
    }

    @Test
    void should_export_on_disk_even_if_remote_isnt_accessible() {
        // Given
        Path expectedFile = temporaryFolder.resolve("backups").resolve("git").resolve("fake").resolve(CONF.name().toLowerCase()).resolve("provider_name").resolve("fake_content.txt");
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(gitClientMock.hasAccess(any())).thenReturn(false);

        GitBackupService sut = new GitBackupService(new RemotesFileRepository(temporaryFolder.toString()), gitClientMock, contentProviders, temporaryFolder.toString());
        try {
            sut.add(remote);
        } catch (Exception e) {
            // ignore
        }

        // When
        sut.backup(remote);

        // Then
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    @Test
    void should_delete_existing_content_before_export() throws IOException {
        // Given
        Path confPath = temporaryFolder.resolve("backups").resolve("git").resolve("fake").resolve(CONF.name().toLowerCase()).resolve("provider_name");
        FileUtils.initFolder(confPath);
        Path fileToDelete = confPath.resolve("to_be_removed.txt");
        Files.write(fileToDelete, "".getBytes());
        
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(gitClientMock.hasAccess(any())).thenReturn(true);

        GitBackupService sut = new GitBackupService(new RemotesFileRepository(temporaryFolder.toString()), gitClientMock, contentProviders, temporaryFolder.toString());

        // When
        sut.backup(remote);

        // Then
        assertThat(Files.exists(fileToDelete)).as("should be delete").isFalse();
    }

    static class FakeProvider implements ChutneyContentProvider {

        @Override
        public String provider() {
            return "provider_name";
        }

        @Override
        public ChutneyContentCategory category() {
            return CONF;
        }

        @Override
        public Stream<ChutneyContent> getContent() {
            return Stream.of(
                ChutneyContent.builder()
                    .withName("fake_content")
                    .withProvider(provider())
                    .withCategory(category())
                    .withFormat("txt")
                    .withContent("fake content")
                    .build()
            );
        }
    }
}
