package com.chutneytesting.execution.api.schedule;

import static java.lang.Thread.sleep;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import com.chutneytesting.server.core.domain.execution.history.PurgeService.PurgeReport;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SchedulePurgeTest {

    @Test
    void return_purge_report() {
        PurgeService mockPurge = mock(PurgeService.class);
        when(mockPurge.purge()).thenReturn(new PurgeReport(Set.of(1L, 2L), Set.of(3L, 4L)));
        SchedulePurge sut = new SchedulePurge(mockPurge, 2, 0);
        assertThat(sut.launchPurge()).hasValue(new PurgeReport(Set.of(1L, 2L), Set.of(3L, 4L)));
        verify(mockPurge).purge();
    }

    @Test
    void handle_runtime_exceptions() {
        PurgeService mockPurge = mock(PurgeService.class);
        when(mockPurge.purge()).thenThrow(new RuntimeException("Purge failed !!"));
        SchedulePurge sut = new SchedulePurge(mockPurge, 1, 0);
        assertThat(sut.launchPurge()).isEmpty();
        verify(mockPurge).purge();
    }

    @Test
    void timeout_on_long_purge() {
        PurgeService mockPurge = mock(PurgeService.class);
        when(mockPurge.purge()).thenAnswer(invocation -> {
            sleep(18000);
            return new PurgeReport(emptySet(), emptySet());
        });
        SchedulePurge sut = new SchedulePurge(mockPurge, 2, 0);
        assertThat(sut.launchPurge()).isEmpty();
        verify(mockPurge).purge();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void retry_on_purge_failure(int retryCount) {
        PurgeService mockPurge = mock(PurgeService.class);
        when(mockPurge.purge()).thenThrow(new RuntimeException("Purge failed !!"));
        SchedulePurge sut = new SchedulePurge(mockPurge, 1, retryCount);
        assertThat(sut.launchPurge()).isEmpty();
        verify(mockPurge, times(retryCount + 1)).purge();
    }
}
