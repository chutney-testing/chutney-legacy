package com.chutneytesting.design.infra.storage.editionlock;

import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.design.domain.editionlock.TestCaseEdition;
import com.chutneytesting.design.domain.editionlock.TestCaseEditions;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCaseEditionsImplTest {

    private TestCaseEditions sut;

    @BeforeEach
    public void before() {
        sut = new TestCaseEditionsImpl(1, TimeUnit.SECONDS.name());
    }

    @Test
    public void should_find_all_previously_added_editions() {
        // Given
        assertThat(sut.add(new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now().minusSeconds(1), "user"))).isTrue();
        assertThat(sut.add(new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now(), "user"))).isTrue();
        // When / Then
        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    public void should_treat_same_object_as_one() {
        // Given
        TestCaseEdition edition = new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now(), "user");
        assertThat(sut.add(edition)).isTrue();
        assertThat(sut.add(edition)).isTrue();
        // When / Then
        assertThat(sut.findAll()).hasSize(1);
    }

    @Test
    public void should_remove_previously_added_editions() {
        // Given
        assertThat(sut.add(new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now().minusSeconds(5), "user"))).isTrue();
        TestCaseEdition toRemoveEdition = new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now(), "user");
        assertThat(sut.add(toRemoveEdition)).isTrue();
        // When
        assertThat(sut.remove(toRemoveEdition)).isTrue();
        // Then
        assertThat(sut.findAll()).hasSize(1);
    }

    @Test
    public void should_respect_given_ttl() {
        // Given
        assertThat(sut.add(new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now().minusSeconds(5), "user"))).isTrue();
        // When
        // Then
        await().atMost(5, SECONDS).untilAsserted(() ->
            assertThat(sut.findAll()).isEmpty()
        );
    }
}
