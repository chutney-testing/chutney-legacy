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

package com.chutneytesting.execution.domain.campaign;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("serial")
public class CampaignAlreadyRunningException extends RuntimeException {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYYMMdd HH:mm:ss");

    public CampaignAlreadyRunningException(CampaignExecution currentReport) {
        super("Campaign [" + currentReport.campaignName + "] is already running since " + currentReport.startDate.format(DATE_TIME_FORMATTER));
    }
}
