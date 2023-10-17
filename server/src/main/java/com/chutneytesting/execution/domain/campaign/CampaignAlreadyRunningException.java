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
