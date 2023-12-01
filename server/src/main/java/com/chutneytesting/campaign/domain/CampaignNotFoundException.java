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

package com.chutneytesting.campaign.domain;

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
