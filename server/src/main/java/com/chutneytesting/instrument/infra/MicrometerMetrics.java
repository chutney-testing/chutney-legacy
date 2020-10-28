package com.chutneytesting.instrument.infra;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class MicrometerMetrics implements ChutneyMetrics {

    private final MeterRegistry meterRegistry;


    MicrometerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void onScenarioExecutionEnded(String scenarioId, List<String> tags, ServerReportStatus status, long duration) {
        String tagsAsString = StringUtils.join(tags, "|");
        Counter scenarioExecutionCount = this.meterRegistry.counter("scenario_execution_count", asList(new ImmutableTag("scenarioId", scenarioId), new ImmutableTag("status", status.name()), new ImmutableTag("tags", tagsAsString)));
        scenarioExecutionCount.increment();

        Timer scenarioExecutionTimer = this.meterRegistry.timer("scenario_execution_timer", asList(new ImmutableTag("scenarioId", scenarioId), new ImmutableTag("tags", tagsAsString)));
        scenarioExecutionTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onCampaignExecutionEnded(String campaignId, ServerReportStatus status, Long campaignDuration, Map<ServerReportStatus, Long> scenarioCountByStatus) {
        Counter scenarioExecutionCount = this.meterRegistry.counter("campaign_execution_count", asList(new ImmutableTag("campaignId", campaignId), new ImmutableTag("status", status.name())));
        scenarioExecutionCount.increment();

        Timer scenarioExecutionTimer = this.meterRegistry.timer("campaign_execution_timer", singleton(new ImmutableTag("campaignId", campaignId)));
        scenarioExecutionTimer.record(campaignDuration, TimeUnit.MILLISECONDS);

        scenarioCountByStatus.entrySet().stream().forEach( e -> {
            Long count = e.getValue();
            String scenarioStatus = e.getKey().name();
            this.meterRegistry.gauge("scenario_in_campaign_gauge", asList(new ImmutableTag("campaignId", campaignId), new ImmutableTag("scenarioStatus", scenarioStatus)), count);
        });
    }


}
