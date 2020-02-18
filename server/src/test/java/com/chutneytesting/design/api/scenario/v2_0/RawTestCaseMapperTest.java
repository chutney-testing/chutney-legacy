package com.chutneytesting.design.api.scenario.v2_0;

import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.mapper.RawTestCaseMapper;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RawTestCaseMapperTest {

    private ImmutableRawTestCaseDto invalid_dto = ImmutableRawTestCaseDto.builder()
        .title("Test mapping")
        .content(" I am invalid\n {").build();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_fail_on_parse_error() {
        expectedException.expect(ScenarioNotParsableException.class);
        RawTestCaseMapper.fromDto(invalid_dto);
    }

}
