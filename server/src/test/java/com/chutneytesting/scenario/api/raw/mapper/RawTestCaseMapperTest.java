package com.chutneytesting.scenario.api.raw.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.scenario.api.raw.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.scenario.api.raw.mapper.RawTestCaseMapper;
import com.chutneytesting.scenario.domain.ScenarioNotParsableException;
import org.junit.jupiter.api.Test;

public class RawTestCaseMapperTest {

    private ImmutableRawTestCaseDto invalid_dto = ImmutableRawTestCaseDto.builder()
        .title("Test mapping")
        .scenario(" I am invalid\n {").build();

    @Test
    public void should_fail_on_parse_error() {
        assertThatThrownBy(() -> RawTestCaseMapper.fromDto(invalid_dto))
            .isInstanceOf(ScenarioNotParsableException.class);
    }

}
