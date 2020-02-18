package com.chutneytesting.design.api.campaign;

import static com.chutneytesting.design.api.campaign.dto.CampaignMapper.fromDto;
import static com.chutneytesting.design.api.campaign.dto.CampaignMapper.toDto;
import static com.chutneytesting.design.api.campaign.dto.CampaignMapper.toDtoWithoutReport;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.isComposableTestCaseId;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.chutneytesting.design.api.campaign.dto.CampaignDto;
import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportDto;
import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportMapper;
import com.chutneytesting.design.api.campaign.dto.CampaignMapper;
import com.chutneytesting.design.api.scenario.v2_0.dto.TestCaseIndexDto;
import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
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
                              CampaignExecutionEngine campaignExecutionEngine, ExecutionHistoryRepository executionHistoryRepository) {
        this.testCaseRepository = testCaseRepository;
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.campaignRepository = campaignRepository;
        this.campaignExecutionEngine = campaignExecutionEngine;
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CampaignDto saveCampaign(@RequestBody CampaignDto campaign) {
        return toDtoWithoutReport(campaignRepository.createOrUpdate(fromDto(campaign)));
    }

    @PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CampaignDto updateCampaign(@RequestBody CampaignDto campaign) {
        return toDtoWithoutReport(campaignRepository.createOrUpdate(fromDto(campaign)));
    }

    @DeleteMapping(path = "/{campaignId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean deleteCampaign(@PathVariable("campaignId") Long campaignId) {
        return campaignRepository.removeById(campaignId);
    }

    @GetMapping(path = "/{campaignId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CampaignDto getCampaignById(@PathVariable("campaignId") Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId);
        List<CampaignExecutionReport> reports = campaignRepository.findExecutionsById(campaignId);
        if(!isEmpty(reports)) {
            sortCampaignExecutionReports(reports);
        }
        Optional<CampaignExecutionReport> currentExecution = campaignExecutionEngine.currentExecution(campaignId);
        if(currentExecution.isPresent()) {
            addCurrentExecution(reports, currentExecution.get());
        }
        return toDto(campaign, reports);
    }

    @GetMapping(path = "/{campaignId}/scenarios", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<TestCaseIndexDto> getCampaignScenarios(@PathVariable("campaignId") Long campaignId) {
        return campaignRepository.findScenariosIds(campaignId).stream()
            .map(id -> {
                if (isComposableTestCaseId(id)) {
                    return composableTestCaseRepository.findById(id).metadata();
                } else {
                    return testCaseRepository.findMetadataById(id);
                }
            })
            .map(meta -> TestCaseIndexDto.from(meta, Collections.emptyList()))
            .collect(Collectors.toList());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CampaignDto> getAllCampaigns() {
        return campaignRepository.findAll().stream()
            .map(CampaignMapper::toDtoWithoutReport)
            .collect(Collectors.toList());
    }

    @GetMapping(path = "/lastexecutions/{limit}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @GetMapping(path = "/scenario/{scenarioId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CampaignDto> getCampaignsByScenarioId(@PathVariable("scenarioId") String scenarioId) {
        return campaignRepository.findCampaignsByScenarioId(scenarioId).stream()
            .map(CampaignMapper::toDtoWithoutReport)
            .collect(Collectors.toList());
    }

    private void addCurrentExecution(List<CampaignExecutionReport> currentCampaignExecutionReports, CampaignExecutionReport campaignExecutionReport) {
        if(currentCampaignExecutionReports == null) {
            currentCampaignExecutionReports = new ArrayList<>();
        }
        currentCampaignExecutionReports.add(0, campaignExecutionReport);
    }

    private void sortCampaignExecutionReports(List<CampaignExecutionReport> listToSort) {
        listToSort.sort(executionComparatorReportByExecutionId());
    }

    private static Comparator<CampaignExecutionReport> executionComparatorReportByExecutionId() {
        return Comparator.<CampaignExecutionReport>comparingLong(value -> value.executionId).reversed();
    }
}
