package com.chutneytesting.environment.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvironmentServiceTest {

    EnvironmentService sut;
    EnvironmentRepository environmentRepository;

    @BeforeEach
    public void setUp() {
        environmentRepository = mock(EnvironmentRepository.class);
        sut = new EnvironmentService(environmentRepository);
    }

    @Test()
    public void create_environment_with_illegal_name_throws() {
        assertThatThrownBy(() -> sut.createEnvironment(Environment.builder().withName("illegal name").withDescription("some description").build()))
            .isInstanceOf(InvalidEnvironmentNameException.class);
    }

    @Test()
    public void update_environment_with_illegal_name_throws() {
        when(environmentRepository.findByName(any())).thenReturn(Environment.builder().withName("OLD_NAME").withDescription("some description").build());

        assertThatThrownBy(() -> sut.updateEnvironment("OLD_NAME", Environment.builder().withName("illegal name").withDescription("some description").build()))
            .isInstanceOf(InvalidEnvironmentNameException.class);
    }
}
