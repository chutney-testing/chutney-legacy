package com.chutneytesting.jira.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableJiraConfigurationDto.class)
@JsonDeserialize(as = ImmutableJiraConfigurationDto.class)
@Value.Style(jdkOnly = true)
public interface JiraConfigurationDto {
    String url();

    String username();

    String password();

  Optional<String> urlProxy();

  Optional<String> userProxy();

  Optional<String> passwordProxy();
}
