package com.chutneytesting.design.api.scenario.v2_0;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.mapper.RawTestCaseMapper;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
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
