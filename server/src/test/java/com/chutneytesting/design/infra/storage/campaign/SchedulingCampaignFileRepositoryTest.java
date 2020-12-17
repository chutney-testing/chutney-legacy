package com.chutneytesting.design.infra.storage.campaign;


import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.campaign.SchedulingCampaign;
import com.chutneytesting.design.domain.campaign.SchedulingCampaignRepository;
import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SchedulingCampaignFileRepositoryTest {

    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    private static SchedulingCampaignRepository sut;
    private static Path SCHEDULING_CAMPAIGN_FILE;

    @BeforeClass
    public static void setUp() throws IOException {
        String tmpConfDir = TEMPORARY_FOLDER.newFolder("conf").getAbsolutePath();
        System.setProperty("configuration-folder", tmpConfDir);
        System.setProperty("persistence-repository-folder", tmpConfDir);

        sut = new SchedulingCampaignFileRepository(tmpConfDir);
        SCHEDULING_CAMPAIGN_FILE = Paths.get(tmpConfDir + "/scheduling/schedulingCampaigns.json");
    }

    @Test
    public void should_add_get_and_remove_scheduled_campaign() {
        //// ADD
        // Given
        SchedulingCampaign sc1 = new SchedulingCampaign(null, 11L, "campaign title 1", LocalDateTime.of(2020, 2, 4, 7, 10));
        SchedulingCampaign sc2 = new SchedulingCampaign(null, 22L, "campaign title 2", LocalDateTime.of(2021, 3, 5, 8, 11));
        SchedulingCampaign sc3 = new SchedulingCampaign(null, 33L, "campaign title 3", LocalDateTime.of(2022, 4, 6, 9, 12));
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
        sut.removeById(2l);

        // Then
        actualContent = FileUtils.readContent(SCHEDULING_CAMPAIGN_FILE);
        assertThat(actualContent).isEqualToIgnoringNewLines(expectedAfterRemove);

        //// GET
        // When
        List<SchedulingCampaign> scheduledCampaigns = sut.getALl();

        // Then
        assertThat(scheduledCampaigns).hasSize(2);
        SchedulingCampaign sc1WithId = new SchedulingCampaign(1L, 11L, "campaign title 1", LocalDateTime.of(2020, 2, 4, 7, 10));
        SchedulingCampaign sc3WithId = new SchedulingCampaign(3L, 33L, "campaign title 3", LocalDateTime.of(2022, 4, 6, 9, 12));

        assertThat(scheduledCampaigns).contains(sc1WithId, sc3WithId);
    }
}
