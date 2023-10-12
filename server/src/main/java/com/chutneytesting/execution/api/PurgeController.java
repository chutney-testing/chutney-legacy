package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import com.chutneytesting.server.core.domain.execution.history.PurgeService.PurgeReport;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/purge")
@RestController
public class PurgeController {
    private final PurgeService purgeService;

    public PurgeController(PurgeService purgeService) {
        this.purgeService = purgeService;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public String defaultPurge() {
        PurgeReport report = purgeService.purge();
        int deletedScenariosExecutionsCount = report.scenariosExecutionsIds().size();
        int deletedCampaignsExecutionsCount = report.campaignsExecutionsIds().size();
        return String.format(
            "{\"purge\": {\"counts\": {\"scenarioExecutions\": %d, \"campaignExecutions\": %d }}}",
            deletedScenariosExecutionsCount, deletedCampaignsExecutionsCount
        );
    }
}
