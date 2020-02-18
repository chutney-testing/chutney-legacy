package com.chutneytesting.instrument.infra;

import static java.util.Arrays.asList;

import com.google.common.collect.Sets;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.instrument.domain.Metrics;
import com.chutneytesting.instrument.infra.storage.MetricsLoader;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
class MicrometerMetrics implements Metrics {

    private final MeterRegistry meterRegistry;

    private final AtomicInteger scenarioTotal;
    private final Timer executionTotalTimer;
    private final Timer executionOkTimer;
    private final Timer executionKoTimer;
    private final MetricsLoader metricsLoader;
    private final AtomicInteger failedScenarios;
    private final AtomicInteger successfulScenarios;
    private Map<String, AtomicInteger> scenarioCountsByTag = new HashMap<>();
    private Map<String, ServerReportStatus> statusByScenarioTitle = new HashMap<>();

    MicrometerMetrics(MeterRegistry meterRegistry, MetricsLoader metricsLoader) {
        this.meterRegistry = meterRegistry;
        this.metricsLoader = metricsLoader;

        scenarioTotal = registerScenarioCountGauge("any", "any");
        failedScenarios = registerScenarioCountGauge("any", "failure");
        successfulScenarios = registerScenarioCountGauge("any", "success");
        ;

        executionTotalTimer = this.meterRegistry.timer("execution_count", Collections.singleton(new ImmutableTag("status", "all")));
        executionOkTimer = this.meterRegistry.timer("execution_count", Collections.singleton(new ImmutableTag("status", "success")));
        executionKoTimer = this.meterRegistry.timer("execution_count", Collections.singleton(new ImmutableTag("status", "failure")));
    }

    @Override
    public void onNewScenario(String scenarioTitle, Collection<String> tags) {
        scenarioTotal.incrementAndGet();
        for (String tag : tags) {
            addCounterIfAbsent(tag);
            scenarioCountsByTag.get(tag).incrementAndGet();
        }
    }

    @Override
    public void onScenarioChange(String scenarioTitle, Collection<String> tags) {
        Map<String, Integer> scenarioCountsByTag = metricsLoader.scenarioCountsByTag();
        scenarioCountsByTag.forEach((tag, count) -> {
            addCounterIfAbsent(tag);
            this.scenarioCountsByTag.get(tag).set(count);
        });
        Sets.difference(scenarioCountsByTag.keySet(), this.scenarioCountsByTag.keySet()).forEach(key -> this.scenarioCountsByTag.get(key).set(0));
    }

    @Override
    public void onExecutionEnded(String scenarioTitle, ServerReportStatus status, long duration) {
        executionTotalTimer.record(duration, TimeUnit.MILLISECONDS);
        if (ServerReportStatus.SUCCESS == status) {
            executionOkTimer.record(duration, TimeUnit.MILLISECONDS);
        } else {
            executionKoTimer.record(duration, TimeUnit.MILLISECONDS);
        }
        statusByScenarioTitle.put(scenarioTitle, status);
        int successfulScenariosCount = Long.valueOf(statusByScenarioTitle.entrySet().stream().filter(e -> ServerReportStatus.SUCCESS == e.getValue()).count()).intValue();
        successfulScenarios.set(successfulScenariosCount);
        int failedScenariosCount = statusByScenarioTitle.size() - successfulScenariosCount;
        failedScenarios.set(failedScenariosCount);
    }

    private void addCounterIfAbsent(String tag) {
        if (!scenarioCountsByTag.containsKey(tag)) {
            scenarioCountsByTag.put(tag, registerScenarioCountGauge(tag, "any"));
        }
    }

    private AtomicInteger registerScenarioCountGauge(String tag, String status) {
        return meterRegistry.gauge("scenario_count", asList(new ImmutableTag("tag", tag), new ImmutableTag("status", status)), new AtomicInteger());
    }
}
