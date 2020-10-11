package com.chutneytesting.design.api.plugins.jira;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableJiraDto.class)
@JsonDeserialize(as = ImmutableJiraDto.class)
@Value.Style(jdkOnly = true)
public interface JiraDto {

    String id();

    String chutneyId();

}
