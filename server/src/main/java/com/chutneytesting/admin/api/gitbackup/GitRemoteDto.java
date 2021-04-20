package com.chutneytesting.admin.api.gitbackup;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGitRemoteDto.class)
@JsonDeserialize(as = ImmutableGitRemoteDto.class)
@Value.Style(jdkOnly = true)
public interface GitRemoteDto {

    String name();

    String url();

    String branch();

    String privateKeyPath();

    String privateKeyPassphrase();

}
