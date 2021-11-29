package com.chutneytesting.design.infra.storage.campaign;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaign;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaignRepository;
import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PeriodicScheduledCampaignFileRepositoryTest {

    private static PeriodicScheduledCampaignRepository sut;
    private static Path SCHEDULING_CAMPAIGN_FILE;

    @BeforeAll
    public static void setUp(@TempDir Path temporaryFolder) throws IOException {
        String tmpConfDir = temporaryFolder.toFile().getAbsolutePath();
        System.setProperty("configuration-folder", tmpConfDir);

        sut = new SchedulingCampaignFileRepository(tmpConfDir);
        SCHEDULING_CAMPAIGN_FILE = Paths.get(tmpConfDir + "/scheduling/schedulingCampaigns.json");
    }

    @Test
    public void should_add_get_and_remove_scheduled_campaign() {
        //// ADD
        // Given
        PeriodicScheduledCampaign sc1 = new PeriodicScheduledCampaign(null, 11L, "campaign title 1", LocalDateTime.of(2020, 2, 4, 7, 10));
        PeriodicScheduledCampaign sc2 = new PeriodicScheduledCampaign(null, 22L, "campaign title 2", LocalDateTime.of(2021, 3, 5, 8, 11));
        PeriodicScheduledCampaign sc3 = new PeriodicScheduledCampaign(null, 33L, "campaign title 3", LocalDateTime.of(2022, 4, 6, 9, 12));
        String expectedAdded =
            "{\n" +
                "  \"1\" : {\n" +
                "    \"id\" : \"1\",\n" +
                "    \"campaignId\" : 11,\n" +
                "    \"campaignTitle\" : \"campaign title 1\",\n" +
                "    \"schedulingDate\" : [ 2020, 2, 4, 7, 10 ]\n" +
                "  },\n" +
                "  \"2\" : {\n" +
                "    \"id\" : \"2\",\n" +
                "    \"campaignId\" : 22,\n" +
                "    \"campaignTitle\" : \"campaign title 2\",\n" +
                "    \"schedulingDate\" : [ 2021, 3, 5, 8, 11 ]\n" +
                "  },\n" +
                "  \"3\" : {\n" +
                "    \"id\" : \"3\",\n" +
                "    \"campaignId\" : 33,\n" +
                "    \"campaignTitle\" : \"campaign title 3\",\n" +
                "    \"schedulingDate\" : [ 2022, 4, 6, 9, 12 ]\n" +
                "  }\n" +
                "}";

        // When
        sut.add(sc1);
        sut.add(sc2);
        sut.add(sc3);

        // Then
        String actualContent = FileUtils.readContent(SCHEDULING_CAMPAIGN_FILE);
        assertThat(actualContent).isEqualToIgnoringNewLines(expectedAdded);

        //// REMOVE
        // Given
        String expectedAfterRemove =
            "{\n" +
                "  \"1\" : {\n" +
                "    \"id\" : \"1\",\n" +
                "    \"campaignId\" : 11,\n" +
                "    \"campaignTitle\" : \"campaign title 1\",\n" +
                "    \"schedulingDate\" : [ 2020, 2, 4, 7, 10 ]\n" +
                "  },\n" +
                "  \"3\" : {\n" +
                "    \"id\" : \"3\",\n" +
                "    \"campaignId\" : 33,\n" +
                "    \"campaignTitle\" : \"campaign title 3\",\n" +
                "    \"schedulingDate\" : [ 2022, 4, 6, 9, 12 ]\n" +
                "  }\n" +
                "}";

        // When
        sut.removeById(2L);

        // Then
        actualContent = FileUtils.readContent(SCHEDULING_CAMPAIGN_FILE);
        assertThat(actualContent).isEqualToIgnoringNewLines(expectedAfterRemove);

        //// GET
        // When
        List<PeriodicScheduledCampaign> periodicScheduledCampaigns = sut.getALl();

        // Then
        assertThat(periodicScheduledCampaigns).hasSize(2);
        PeriodicScheduledCampaign sc1WithId = new PeriodicScheduledCampaign(1L, 11L, "campaign title 1", LocalDateTime.of(2020, 2, 4, 7, 10));
        PeriodicScheduledCampaign sc3WithId = new PeriodicScheduledCampaign(3L, 33L, "campaign title 3", LocalDateTime.of(2022, 4, 6, 9, 12));

        assertThat(periodicScheduledCampaigns).contains(sc1WithId, sc3WithId);
    }

    @Test
    void should_read_and_write_concurrently() throws InterruptedException {
        List<Exception> exceptions = new ArrayList<>();
        Runnable addScheduledCampaign = () -> {
            try {
                sut.add(new PeriodicScheduledCampaign(null, 11L, "campaign title 1", LocalDateTime.now().minusWeeks(1)));
            } catch (Exception e) {
                exceptions.add(e);
            }
        };
        Runnable readScheduledCampaigns = () -> {
            try {
                sut.getALl();
            } catch (Exception e) {
                exceptions.add(e);
            }
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        IntStream.range(1, 5).forEach((i) -> {
            pool.submit(addScheduledCampaign);
            pool.submit(readScheduledCampaigns);
        });
        pool.shutdown();
        if (pool.awaitTermination(5, TimeUnit.SECONDS)) {
            assertThat(exceptions).isEmpty();
        } else {
            fail("Pool termination timeout ...");
        }
    }
}
