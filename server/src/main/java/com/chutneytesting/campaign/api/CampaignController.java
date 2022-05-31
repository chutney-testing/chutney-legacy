package com.chutneytesting.campaign.api;

import static com.chutneytesting.campaign.api.dto.CampaignMapper.fromDto;
import static com.chutneytesting.campaign.api.dto.CampaignMapper.toDto;
import static com.chutneytesting.campaign.api.dto.CampaignMapper.toDtoWithoutReport;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.chutneytesting.campaign.api.dto.CampaignDto;
import com.chutneytesting.campaign.api.dto.CampaignExecutionReportDto;
import com.chutneytesting.campaign.api.dto.CampaignExecutionReportMapper;
import com.chutneytesting.campaign.api.dto.CampaignMapper;
import com.chutneytesting.campaign.domain.Campaign;
import com.chutneytesting.campaign.domain.CampaignExecutionReport;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.scenario.api.raw.dto.TestCaseIndexDto;
import com.chutneytesting.scenario.domain.ComposableTestCaseRepository;
import com.chutneytesting.scenario.domain.TestCaseRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ui/campaign/v1")
@CrossOrigin(origins = "*")
public class CampaignController {

    private final TestCaseRepository testCaseRepository;
    private final ComposableTestCaseRepository composableTestCaseRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignExecutionEngine campaignExecutionEngine;

    public CampaignController(TestCaseRepository testCaseRepository,
                              ComposableTestCaseRepository composableTestCaseRepository,
                              CampaignRepository campaignRepository,
                              CampaignExecutionEngine campaignExecutionEngine) {
        this.testCaseRepository = testCaseRepository;
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.campaignRepository = campaignRepository;
        this.campaignExecutionEngine = campaignExecutionEngine;
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignDto saveCampaign(@RequestBody CampaignDto campaign) {
        return toDtoWithoutReport(campaignRepository.createOrUpdate(fromDto(campaign)));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignDto updateCampaign(@RequestBody CampaignDto campaign) {
        return toDtoWithoutReport(campaignRepository.createOrUpdate(fromDto(campaign)));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @DeleteMapping(path = "/{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteCampaign(@PathVariable("campaignId") Long campaignId) {
        return campaignRepository.removeById(campaignId);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = "/{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignDto getCampaignById(@PathVariable("campaignId") Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId);
        List<CampaignExecutionReport> reports = campaignRepository.findExecutionsById(campaignId);
        if (!isEmpty(reports)) {
            reports.sort(CampaignExecutionReport.executionIdComparator().reversed());
        }
        campaignExecutionEngine.currentExecution(campaignId)
            .ifPresent(report -> addCurrentExecution(reports, report));
        return toDto(campaign, reports);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = "/{campaignId}/scenarios", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestCaseIndexDto> getCampaignScenarios(@PathVariable("campaignId") Long campaignId) {
        return campaignRepository.findScenariosIds(campaignId).stream()
            .map(id -> {
                if (isComposableDomainId(id)) {
                    return composableTestCaseRepository.findById(id).metadata();
                } else {
                    return testCaseRepository.findMetadataById(id);
                }
            })
            .map(meta -> TestCaseIndexDto.from(meta, Collections.emptyList()))
            .collect(Collectors.toList());
    }

    static boolean isComposableDomainId(String testCaseId) {
        return testCaseId.contains("-"); //return testCaseId.contains("#") && testCaseId.contains(":");
    }
    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CampaignDto> getAllCampaigns() {
        return campaignRepository.findAll().stream()
            .map(CampaignMapper::toDtoWithoutReport)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = "/lastexecutions/{limit}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CampaignExecutionReportDto> getLastExecutions(@PathVariable("limit") Long limit) {
        List<CampaignExecutionReport> lastExecutions = campaignExecutionEngine.currentExecutions();

        // Complete current executions with finished ones up to the limit
        if (lastExecutions.size() < limit) {
            lastExecutions.addAll(campaignRepository.findLastExecutions(limit - lastExecutions.size()));
        }

        return lastExecutions.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .sorted(Comparator.comparing(value -> ((CampaignExecutionReportDto) value).getStartDate()).reversed())
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/scenario/{scenarioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CampaignDto> getCampaignsByScenarioId(@PathVariable("scenarioId") String scenarioId) {
        return campaignRepository.findCampaignsByScenarioId(scenarioId).stream()
            .map(CampaignMapper::toDtoWithoutReport)
            .collect(Collectors.toList());
    }

    private void addCurrentExecution(List<CampaignExecutionReport> currentCampaignExecutionReports, CampaignExecutionReport campaignExecutionReport) {
        if (currentCampaignExecutionReports == null) {
            currentCampaignExecutionReports = new ArrayList<>();
        }
        currentCampaignExecutionReports.add(0, campaignExecutionReport);
    }
}
