package com.chutneytesting.design.api.testcase;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTestCaseEditionDto.class)
@JsonDeserialize(as = ImmutableTestCaseEditionDto.class)
@Value.Style(jdkOnly = true)
public interface TestCaseEditionDto {

    String testCaseId();

    Integer testCaseVersion();

    Instant editionStartDate();

    String editionUser();
}
