package com.chutneytesting.environment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import java.util.List;
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

    @Test
    void create_environment_should_throw_when_env_already_exist() {
        // Given
        when(environmentRepository.listNames())
            .thenReturn(List.of("EXISTING"));

        // Then
        assertThatThrownBy(() -> sut.createEnvironment(Environment.builder().withName("EXISTING").build()))
            .isInstanceOf(AlreadyExistingEnvironmentException.class);
    }

    @Test
    void create_environment_not_throw_when_env_already_exist_but_is_forced() {
        // Given
        Environment expected = Environment.builder().withName("EXISTING").build();
        when(environmentRepository.listNames())
            .thenReturn(List.of("EXISTING"));

        // When
        Environment actual = sut.createEnvironment(expected, true);

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
