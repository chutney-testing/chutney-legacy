package com.chutneytesting.design.api.plugins.jira;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableJiraConfigurationDto.class)
@JsonDeserialize(as = ImmutableJiraConfigurationDto.class)
@Value.Style(jdkOnly = true)
public interface JiraConfigurationDto {
    String url();

    String username();

    String password();
}
