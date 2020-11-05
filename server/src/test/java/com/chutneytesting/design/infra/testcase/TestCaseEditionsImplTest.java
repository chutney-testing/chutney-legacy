package com.chutneytesting.design.infra.testcase;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.testcase.TestCaseEdition;
import com.chutneytesting.design.domain.testcase.TestCaseEditions;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TestCaseEditionsImplTest {

    private TestCaseEditions sut;

    @Before
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
    public void should_respect_given_ttl() throws InterruptedException {
        // Given
        assertThat(sut.add(new TestCaseEdition(TestCaseMetadataImpl.builder().build(), now().minusSeconds(5), "user"))).isTrue();
        // When
        TimeUnit.MILLISECONDS.sleep(1500);
        // Then
        assertThat(sut.findAll()).isEmpty();
    }
}
