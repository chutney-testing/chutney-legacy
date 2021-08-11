package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerSummaryTask.OUTPUT_SUMMARY;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.buildMeterName;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class MicrometerSummaryTaskTest {

    private MicrometerSummaryTask sut;
    private final String METER_NAME_PREFIX = "summaryName";

    @Test
    public void summary_name_is_mandatory_if_no_given_summary() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void summary_buffer_length_must_be_an_integer() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, "not a integer", null, null, null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_expiry_must_be_a_duration() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, "not a duration", null, null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void summary_max_value_must_be_a_long() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, "not a long", null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_min_value_must_be_a_long() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, "not a long", null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_percentile_precision_must_be_an_integer() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, "not a integer", null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_percentiles_must_be_a_list_of_double() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, "not a list of double", null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_scale_must_be_a_double() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, "not a double", null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_sla_must_be_a_list_of_long() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, "not a list of long", null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void summary_record_must_be_a_double() {
        assertThatThrownBy(() ->
            new MicrometerSummaryTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, null, null, null, "not a double")
        ).isExactlyInstanceOf(NumberFormatException.class);
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
        sut = new MicrometerSummaryTask(logger, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, null, null, registry, "6");

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
