/*
 *  Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.chutneytesting.campaign.infra;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO : Implemented for retro compatibility - to remove in future version
class SchedulingCampaignsDtoDeserializer extends StdDeserializer<SchedulingCampaignDto> {

    protected SchedulingCampaignsDtoDeserializer() {
        super(SchedulingCampaignDto.class);
    }

    @Override
    public SchedulingCampaignDto deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();

        String id = node.get("id").asText();
        List<Long> campaignsId;
        List<String> campaignsTitle;
        LocalDateTime schedulingDate = getSchedulingDate(node);
        String frequency = getFrequency(node);

        if (node.has("campaignId")) {
            campaignsId = List.of(node.get("campaignId").asLong());
        } else {
            campaignsId = getCampaignsId(node.get("campaignsId"));
        }

        if (node.has("campaignTitle")) {
            campaignsTitle = List.of(node.get("campaignTitle").asText());
        } else {
            campaignsTitle = getCampaignsTitle(node.get("campaignsTitle"));
        }

        return new SchedulingCampaignDto(id, campaignsId, campaignsTitle, schedulingDate, frequency);
    }

    private List<Long> getCampaignsId(JsonNode node) {
        List<Long> campaignsId = new ArrayList<>();
        for (JsonNode id : node) {
            campaignsId.add(id.asLong());
        }
        return campaignsId;
    }

    private List<String> getCampaignsTitle(JsonNode node) {
        List<String> campaignsTitle = new ArrayList<>();
        for (JsonNode title : node) {
            campaignsTitle.add(title.asText());
        }
        return campaignsTitle;
    }

    private LocalDateTime getSchedulingDate(JsonNode node) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return objectMapper.readValue(node.get("schedulingDate").toString(), LocalDateTime.class);
    }

    private String getFrequency(JsonNode node) throws JsonProcessingException {
        return node.has("frequency") ? node.get("frequency").asText() : null;
    }
}
