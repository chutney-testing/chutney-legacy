package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerSummaryTask.OUTPUT_SUMMARY;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.buildMeterName;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class MicrometerSummaryTaskTest {

    private MicrometerSummaryTask sut;
    private final String METER_NAME_PREFIX = "summaryName";

    @Test
    public void summary_name_is_mandatory_if_no_given_summary() {
        MicrometerSummaryTask micrometerSummaryTask = new MicrometerSummaryTask(null, null, null, null, null, "not a integer", "not a duration", "not a long", "not a long", "not a integer", null, "not a list of double", "not a double", "not a list of long", null, null, "not a double");
        List<String> errors = micrometerSummaryTask.validateInputs();

        assertThat(errors.size()).isEqualTo(10);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors.get(0)).isEqualTo("name and distributionSummary cannot be both null");
        softly.assertThat(errors.get(1)).isEqualTo("[bufferLength parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a integer\")");
        softly.assertThat(errors.get(2)).isEqualTo("[percentilePrecision parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a integer\")");
        softly.assertThat(errors.get(3)).isEqualTo("[maxValue parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a long\")");
        softly.assertThat(errors.get(4)).isEqualTo("[minValue parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a long\")");
        softly.assertThat(errors.get(5)).isEqualTo("[scale parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a double\")");
        softly.assertThat(errors.get(6)).isEqualTo("[record parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a double\")");
        softly.assertThat(errors.get(7)).startsWith("[expiry is not parsable] not applied because of exception java.lang.IllegalArgumentException(Cannot parse duration: not a duration");
        softly.assertThat(errors.get(8)).isEqualTo("[Cannot parse percentils list] not applied because of exception java.lang.NumberFormatException(For input string: \"not a list of double\")");
        softly.assertThat(errors.get(9)).isEqualTo("[Cannot parse sla list] not applied because of exception java.lang.NumberFormatException(For input string: \"not a list of long\")");
        softly.assertAll();
    }

    @Test
    public void should_create_micrometer_summary() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerSummaryTask(new TestLogger(), meterName, null, null, null, null, null, null, null, null, null, null, null, null, null, registry, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSummaryObjectType(result);

        DistributionSummary outputSummary = (DistributionSummary) result.outputs.get(OUTPUT_SUMMARY);
        assertThat(registry.find(meterName).summary()).isEqualTo(outputSummary);
        assertThat(outputSummary.totalAmount()).isEqualTo(0);
        assertThat(outputSummary.max()).isEqualTo(0);
        assertThat(outputSummary.mean()).isEqualTo(0);
        assertThat(outputSummary.count()).isEqualTo(0);
    }

    @Test
    public void should_create_micrometer_summary_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerSummaryTask(new TestLogger(), meterName, "description", "my unit", Lists.list("tag", "my tag value"), null, null, null, null, null, null, null, null, null, null, givenMeterRegistry, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSummaryObjectType(result);

        DistributionSummary outputSummary = (DistributionSummary) result.outputs.get(OUTPUT_SUMMARY);
        assertThat(globalRegistry.find(meterName).summaries()).isEmpty();
        assertThat(givenMeterRegistry.find(meterName).summary()).isEqualTo(outputSummary);
        assertThat(outputSummary.getId().getDescription()).isEqualTo("description");
        assertThat(outputSummary.getId().getBaseUnit()).isEqualTo("my unit");
        assertThat(outputSummary.getId().getTag("tag")).isEqualTo("my tag value");
    }

    @Test
    public void should_create_micrometer_summary_and_record_an_event() {
        // Given
        sut = new MicrometerSummaryTask(new TestLogger(), buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, null, null, new SimpleMeterRegistry(), "3.2");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSummaryObjectType(result);

        DistributionSummary outputSummary = (DistributionSummary) result.outputs.get(OUTPUT_SUMMARY);
        assertThat(outputSummary.totalAmount()).as("summary totalAmount").isEqualTo(3.2);
        assertThat(outputSummary.max()).as("summary max").isEqualTo(3.2);
        assertThat(outputSummary.mean()).as("summary mean").isEqualTo(3.2);
        assertThat(outputSummary.count()).isEqualTo(1);
    }

    @Test
    public void should_record_an_event_with_given_summary() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        DistributionSummary givenSummary = registry.summary(buildMeterName(METER_NAME_PREFIX));
        givenSummary.record(3);
        sut = new MicrometerSummaryTask(new TestLogger(), null, null, null, null, null, null, null, null, null, null, null, null, null, givenSummary, registry, "6.8");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSummaryObjectType(result);

        DistributionSummary outputSummary = (DistributionSummary) result.outputs.get(OUTPUT_SUMMARY);
        assertThat(outputSummary).isEqualTo(givenSummary);
        assertThat(outputSummary.totalAmount()).isEqualTo(9.8);
        assertThat(outputSummary.max()).isEqualTo(6.8);
        assertThat(outputSummary.mean()).isEqualTo(4.9);
        assertThat(outputSummary.count()).isEqualTo(2);
    }

    @Test
    public void should_log_summary_record_total_max_mean_and_count() {
        // Given
        TestLogger logger = new TestLogger();
        MeterRegistry registry = new SimpleMeterRegistry();
        sut = new MicrometerSummaryTask(logger, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, null, null, registry, "6.0");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSummaryObjectType(result);

        assertThat(logger.info).hasSize(5);
        assertThat(logger.info.get(0)).isEqualTo("Distribution summary updated by 6.0");
        assertThat(logger.info.get(1)).isEqualTo("Distribution summary current total is 6.0");
        assertThat(logger.info.get(2)).isEqualTo("Distribution summary current max is 6.0");
        assertThat(logger.info.get(3)).isEqualTo("Distribution summary current mean is 6.0");
        assertThat(logger.info.get(4)).isEqualTo("Distribution summary current count is 1");
    }

    private void assertSuccessAndSummaryObjectType(TaskExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_SUMMARY, DistributionSummary.class);
    }
}
