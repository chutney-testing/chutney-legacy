package com.chutneytesting.execution.domain.campaign;

import com.chutneytesting.campaign.domain.CampaignExecutionReport;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("serial")
public class CampaignAlreadyRunningException extends RuntimeException {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYYMMdd HH:mm:ss");

    public CampaignAlreadyRunningException(CampaignExecutionReport currentReport) {
        super("Campaign [" + currentReport.campaignName + "] is already running since " + currentReport.startDate.format(DATE_TIME_FORMATTER));
    }
}
