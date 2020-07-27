package com.chutneytesting.design.domain.scenario;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TestCaseMetadata {

    String id(); // TODO - to extract

    Optional<String> datasetId();

    String title();

    String description();

    Instant creationDate();

    List<String> tags();

    String repositorySource(); // TODO - to delete

}
