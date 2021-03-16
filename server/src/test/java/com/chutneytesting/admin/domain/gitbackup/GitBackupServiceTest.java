package com.chutneytesting.admin.domain.gitbackup;

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
import java.util.List;
import org.junit.jupiter.api.Test;

class GitBackupServiceTest {

    private final Remotes remotesMock = mock(RemotesFileRepository.class);
    private final GitClient clientMock = mock(GitClientImpl.class);

    @Test
    void should_remove_passphrase_when_asked_for_remotes() {
        RemoteRepository remote = new RemoteRepository("name", "u.r/l", "branch", "path/to/private/key", "passphrase");
        when(remotesMock.getAll()).thenReturn(singletonList(remote));

        GitBackupService sut = new GitBackupService(remotesMock, clientMock);

        // When
        List<RemoteRepository> all = sut.getAll();

        // Then
        assertThat(all.get(0).privateKeyPassphrase).isNullOrEmpty();
    }

    @Test
    void should_throw_when_adding_a_remote_not_accessible() {
        // Given
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(clientMock.hasAccess(any())).thenReturn(false);

        GitBackupService sut = new GitBackupService(remotesMock, clientMock);

        // When & Then
        assertThatThrownBy(() -> sut.add(remote))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_add_when_adding_an_accessible_remote() {
        // Given
        RemoteRepository remote = new RemoteRepository("fake", "fake", "fake", "fake", "fake");
        when(clientMock.hasAccess(any())).thenReturn(true);

        GitBackupService sut = new GitBackupService(remotesMock, clientMock);

        // When
        sut.add(remote);

        // Then
        verify(remotesMock, times(1)).add(remote);
    }

}
