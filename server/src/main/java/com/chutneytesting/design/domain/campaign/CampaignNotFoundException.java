package com.chutneytesting.design.domain.campaign;

/**
 * To be caught by <b>spring</b> fault-barrier and processed by <b>spring-boot</b> error handler.
 */
@SuppressWarnings("serial")
public class CampaignNotFoundException extends RuntimeException {
    public static final String NOT_FOUND_MESSAGE = "Given ID does not match any campaign";

    public CampaignNotFoundException(Long campaignId) {
        super(NOT_FOUND_MESSAGE + ": campaignId=" + campaignId);
    }
}
