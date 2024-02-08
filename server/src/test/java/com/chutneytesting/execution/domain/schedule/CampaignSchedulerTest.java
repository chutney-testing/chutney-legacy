/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.execution.domain.schedule;

import static com.chutneytesting.campaign.domain.Frequency.EMPTY;
import static com.chutneytesting.execution.domain.schedule.CampaignScheduler.SCHEDULER_EXECUTE_USER;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.Frequency;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaign;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaignRepository;
import com.chutneytesting.campaign.infra.SchedulingCampaignFileRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.tools.file.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;

public class CampaignSchedulerTest {

    private CampaignScheduler sut;

    private final CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository = mock(PeriodicScheduledCampaignRepository.class);

    private static Path SCHEDULING_CAMPAIGN_FILE;
    @TempDir
    private static Path temporaryFolder;
    private final Clock clock = mock(Clock.class);

    @BeforeEach
    public void setUp() {
        Clock fixedClock = Clock.fixed(LocalDateTime.of(2024, 3, 15, 15, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        sut = new CampaignScheduler(campaignExecutionEngine, clock, periodicScheduledCampaignRepository, Executors.newFixedThreadPool(2));
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_execute_campaign_as_internal_user_named_auto_when_executing_periodic_scheduled_campaign(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(campaignExecutionEngine).executeById(periodicScheduledCampaign.get(0).campaignsId.get(0), "auto");
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_remove_last_execution_when_executing_periodic_scheduled_campaign(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(periodicScheduledCampaignRepository).removeById(periodicScheduledCampaign.get(0).id);
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_add_next_execution_when_executing_periodic_scheduled_campaign_except_for_EMPTY_frequency(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        if (EMPTY.equals(frequency)) {
            verify(periodicScheduledCampaignRepository, times(0)).add(any());
        } else {
            verify(periodicScheduledCampaignRepository).add(
                periodicScheduledCampaign.get(0).nextScheduledExecution()
            );
        }
    }

    @Test
    void should_reschedule_missed_campaign() {
        String tmpConfDir = temporaryFolder.toFile().getAbsolutePath();
        SCHEDULING_CAMPAIGN_FILE = Paths.get(tmpConfDir + "/scheduling/schedulingCampaigns.json");
        CampaignScheduler campaignScheduler = new CampaignScheduler(campaignExecutionEngine, clock, new SchedulingCampaignFileRepository(tmpConfDir), Executors.newFixedThreadPool(2));

        String initialSchedules = """
                {
                      "1" : {
                        "id" : "1",
                        "campaignsId" : [ 11 ],
                        "campaignsTitle" : [ "campaign title 1" ],
                        "schedulingDate" : [ 2024, 1, 1, 14, 0 ],
                        "frequency" : "Weekly"
                      },
                      "2" : {
                        "id" : "2",
                        "campaignsId" : [ 22 ],
                        "campaignsTitle" : [ "campaign title 2" ],
                        "schedulingDate" : [ 2023, 3, 4, 7, 10 ],
                        "frequency" : "Hourly"
                      },
                      "3" : {
                        "id" : "3",
                        "campaignsId" : [ 33 ],
                        "campaignsTitle" : [ "campaign title 3" ],
                        "schedulingDate" : [ 2024, 2, 2, 14, 0 ]
                      }
                }
                """;
        FileUtils.initFolder(SCHEDULING_CAMPAIGN_FILE.getParent());
        FileUtils.writeContent(SCHEDULING_CAMPAIGN_FILE, initialSchedules);


        String expectedSchedules =
          """
          {
            "4" : {
              "id" : "4",
              "campaignsId" : [ 11 ],
              "campaignsTitle" : [ "campaign title 1" ],
              "schedulingDate" : [ 2024, 3, 18, 14, 0 ],
              "frequency" : "Weekly"
            },
            "5" : {
              "id" : "5",
              "campaignsId" : [ 22 ],
              "campaignsTitle" : [ "campaign title 2" ],
              "schedulingDate" : [ 2024, 3, 15, 16, 10 ],
              "frequency" : "Hourly"
            }
          }
          """;

        // WHEN
        campaignScheduler.scheduledMissedCampaignToExecute();

        // THEN
        String result = FileUtils.readContent(SCHEDULING_CAMPAIGN_FILE);
        assertThat(result).isEqualToIgnoringNewLines(expectedSchedules);

    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_retrieving_campaigns_to_execute() {
        when(periodicScheduledCampaignRepository.getALl())
            .thenThrow(new RuntimeException("scheduledCampaignRepository.getAll()"));
        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(periodicScheduledCampaignRepository).getALl();
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_executing_campaigns() {
        List<PeriodicScheduledCampaign> periodicScheduledCampaigns = createPeriodicScheduledCampaigns(asList(Frequency.MONTHLY, Frequency.DAILY));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaigns
            );
        when(campaignExecutionEngine.executeById(periodicScheduledCampaigns.get(0).campaignsId.get(0), SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(campaignExecutionEngine, times(periodicScheduledCampaigns.size())).executeById(any(), any());
    }

    @Test
    void should_execute_sequentially_when_executing_periodic_scheduled_campaigns() {
        PeriodicScheduledCampaign sc1 = new PeriodicScheduledCampaign(1L, List.of(11L, 22L), List.of("cpg 11", "cpg 22"), now(clock).minusSeconds(5), Frequency.HOURLY);

        List<PeriodicScheduledCampaign> periodicScheduledCampaign = List.of(sc1);
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );
        InOrder inOrder = inOrder(campaignExecutionEngine);

        sut.executeScheduledCampaigns();

        inOrder.verify(campaignExecutionEngine).executeById(periodicScheduledCampaign.get(0).campaignsId.get(0), "auto");
        inOrder.verify(campaignExecutionEngine).executeById(periodicScheduledCampaign.get(0).campaignsId.get(1), "auto");
        verify(campaignExecutionEngine, times(2)).executeById(any(), any());
    }

    private List<PeriodicScheduledCampaign> createPeriodicScheduledCampaigns(List<Frequency> frequencies) {
        Random rand = new Random();
        return frequencies.stream()
            .map(f ->
                new PeriodicScheduledCampaign(rand.nextLong(), List.of(rand.nextLong()), List.of("title"), now(clock).minusSeconds(5), f)
            )
            .collect(toList());
    }
}
