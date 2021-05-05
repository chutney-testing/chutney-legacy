package com.chutneytesting.execution.api;

import static com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportMapper.toDto;

import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportDto;
import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportMapper;
import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.execution.api.report.surefire.SurefireCampaignExecutionReportBuilder;
import com.chutneytesting.execution.api.report.surefire.SurefireScenarioExecutionReportBuilder;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.security.domain.UserService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CampaignExecutionUiController.BASE_URL)
@CrossOrigin(origins = "*")
public class CampaignExecutionUiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignExecutionUiController.class);
    static final String BASE_URL = "/api/ui/campaign/execution/v1";

    private final CampaignExecutionEngine campaignExecutionEngine;
    private final SurefireCampaignExecutionReportBuilder surefireCampaignExecutionReportBuilder;
    private final CampaignRepository campaignRepository;
    private final UserService userService;

    public CampaignExecutionUiController(CampaignExecutionEngine campaignExecutionEngine, SurefireScenarioExecutionReportBuilder surefireScenarioExecutionReportBuilder, CampaignRepository campaignRepository, UserService userService) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.surefireCampaignExecutionReportBuilder = new SurefireCampaignExecutionReportBuilder(surefireScenarioExecutionReportBuilder);
        this.campaignRepository = campaignRepository;
        this.userService = userService;
    }

    @GetMapping(path = {"/{campaignName}", "/{campaignName}/{env}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CampaignExecutionReportDto> executeCampaignByName(@PathVariable("campaignName") String campaignName, @PathVariable("env") Optional<String> environment) {
        List<CampaignExecutionReport> reports;
        String userId = userService.getCurrentUser().getId();
        if (environment.isPresent()) {
            reports = campaignExecutionEngine.executeByName(campaignName, environment.get(), userId);
        } else {
            reports = campaignExecutionEngine.executeByName(campaignName, userId);
        }
        return reports.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @PostMapping(path = {"/replay/{campaignExecutionId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignExecutionReportDto replayFailedScenario(@PathVariable("campaignExecutionId") Long campaignExecutionId) {
        CampaignExecutionReport campaignExecutionReport = campaignRepository.findByExecutionId(campaignExecutionId);
        String userId = userService.getCurrentUser().getId();
        List<String> failedIds = campaignExecutionReport.scenarioExecutionReports().stream().filter(s -> !ServerReportStatus.SUCCESS.equals(s.execution.status())).map(s -> s.scenarioId).collect(Collectors.toList());
        Campaign campaign = campaignRepository.findById(campaignExecutionReport.campaignId);
        campaign.executionEnvironment(campaignExecutionReport.executionEnvironment);
        CampaignExecutionReport newExecution = campaignExecutionEngine.executeScenarioInCampaign(failedIds, campaign, userId);
        return toDto(newExecution);
    }

    @GetMapping(path = {"/{campaignPattern}/surefire", "/{campaignPattern}/surefire/{env}"}, produces = "application/zip")
    public byte[] executeCampaignsByPatternWithSurefireReport(HttpServletResponse response, @PathVariable("campaignPattern") String campaignPattern, @PathVariable("env") Optional<String> environment) {
        String userId = userService.getCurrentUser().getId();
        response.addHeader("Content-Disposition", "attachment; filename=\"surefire-report.zip\"");
        List<CampaignExecutionReport> reports;
        if (environment.isPresent()) {
            reports = campaignExecutionEngine.executeByName(campaignPattern, environment.get(), userId);
        } else {
            reports = campaignExecutionEngine.executeByName(campaignPattern, userId);
        }
        return surefireCampaignExecutionReportBuilder.createReport(reports);
    }

    @PostMapping(path = "/{executionId}/stop")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void stopExecution(@PathVariable("executionId") Long executionId) {
        LOGGER.debug("Stop campaign execution {}", executionId);
        campaignExecutionEngine.stopExecution(executionId);
    }

    @GetMapping(path = {"/byID/{campaignId}", "/byID/{campaignId}/{env}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignExecutionReportDto executeCampaignById(@PathVariable("campaignId") Long campaignId, @PathVariable("env") Optional<String> environment) {
        String userId = userService.getCurrentUser().getId();
        CampaignExecutionReport report;
        if (environment.isPresent()) {
            report = campaignExecutionEngine.executeById(campaignId, environment.get(), userId);
        } else {
            report = campaignExecutionEngine.executeById(campaignId, userId);
        }
        return toDto(report);
    }
}
