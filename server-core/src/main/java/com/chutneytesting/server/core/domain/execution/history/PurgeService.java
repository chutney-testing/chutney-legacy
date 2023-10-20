package com.chutneytesting.server.core.domain.execution.history;

import java.util.Set;

public interface PurgeService {

    PurgeReport purge();

    record PurgeReport(Set<Long> scenariosExecutionsIds, Set<Long> campaignsExecutionsIds) {
        public PurgeReport {
            scenariosExecutionsIds = Set.copyOf(scenariosExecutionsIds);
            campaignsExecutionsIds = Set.copyOf(campaignsExecutionsIds);
        }
    }
}
