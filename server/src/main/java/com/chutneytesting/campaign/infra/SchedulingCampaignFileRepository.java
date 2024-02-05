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

package com.chutneytesting.campaign.infra;

import static com.chutneytesting.ServerConfigurationValues.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.campaign.domain.Frequency.toFrequency;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.campaign.domain.PeriodicScheduledCampaign;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaignRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Scheduling campaign persistence.
 */
@Repository
public class SchedulingCampaignFileRepository implements PeriodicScheduledCampaignRepository {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("scheduling");
    private static final String SCHEDULING_CAMPAIGNS_FILE = "schedulingCampaigns.json";

    private final Path storeFolderPath;
    private final Path resolvedFilePath;
    private final AtomicLong currentMaxId = new AtomicLong();

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .registerModule(new JavaTimeModule())
        .registerModule(new SimpleModule()
            .addDeserializer(SchedulingCampaignDto.class, new SchedulingCampaignsDtoDeserializer()))
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final ReadWriteLock rwLock;

    SchedulingCampaignFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.rwLock = new ReentrantReadWriteLock(true);
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        this.resolvedFilePath = this.storeFolderPath.resolve(SCHEDULING_CAMPAIGNS_FILE);
        initFolder(this.storeFolderPath);
        currentMaxId.set(this.getALl().stream().map(sm -> sm.id).max(Long::compare).orElse(0L));
    }

    @Override
    public PeriodicScheduledCampaign add(PeriodicScheduledCampaign periodicScheduledCampaign) {
        final Lock writeLock;
        (writeLock = rwLock.writeLock()).lock();
        try {
            Map<String, SchedulingCampaignDto> schedulingCampaigns = readFromDisk();
            long id = currentMaxId.incrementAndGet();
            schedulingCampaigns.put(String.valueOf(id), toDto(id, periodicScheduledCampaign));
            writeOnDisk(resolvedFilePath, schedulingCampaigns);

            return periodicScheduledCampaign;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeById(Long id) {
        final Lock writeLock;
        (writeLock = rwLock.writeLock()).lock();
        try {
            Map<String, SchedulingCampaignDto> schedulingCampaigns = readFromDisk();
            schedulingCampaigns.remove(String.valueOf(id));
            writeOnDisk(resolvedFilePath, schedulingCampaigns);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeCampaignId(Long id) {
        final Lock writeLock;
        (writeLock = rwLock.writeLock()).lock();
        try {
            Map<String, SchedulingCampaignDto> schedulingCampaigns = readFromDisk();
            Map<String, SchedulingCampaignDto> schedulingCampaignsFiltered = new HashMap<>();
            schedulingCampaigns.forEach((key, schedulingCampaignDto) -> {
                int indexCampaignId = schedulingCampaignDto.campaignsId.indexOf(id);
                if (indexCampaignId != -1) { // Remove id and title if the campaignId has been found
                    schedulingCampaignDto.campaignsTitle.remove(indexCampaignId);
                    schedulingCampaignDto.campaignsId.remove(indexCampaignId);
                }
                if (!schedulingCampaignDto.campaignsId.isEmpty()) { // Set the schedule only if a campaign is present after removal
                    schedulingCampaignsFiltered.put(key, schedulingCampaignDto);
                }
            });
            writeOnDisk(resolvedFilePath, schedulingCampaignsFiltered);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<PeriodicScheduledCampaign> getALl() {
        return readFromDisk().values().stream()
            .map(this::fromDto)
            .collect(Collectors.toList());
    }

    private Map<String, SchedulingCampaignDto> readFromDisk() {
        Map<String, SchedulingCampaignDto> stringSchedulingCampaignDTO = new HashMap<>();
        final Lock readLock;
        (readLock = rwLock.readLock()).lock();
        try {
            if (Files.exists(resolvedFilePath)) {
                byte[] bytes = Files.readAllBytes(resolvedFilePath);
                stringSchedulingCampaignDTO.putAll(objectMapper.readValue(bytes, new TypeReference<HashMap<String, SchedulingCampaignDto>>() {
                }));
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + resolvedFilePath, e);
        } finally {
            readLock.unlock();
        }

        return stringSchedulingCampaignDTO;
    }

    private void writeOnDisk(Path filePath, Map<String, SchedulingCampaignDto> schedulingCampaignDTO) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(schedulingCampaignDTO);
            try {
                Files.write(filePath, bytes);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot write in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + schedulingCampaignDTO, e);
        }
    }

    private PeriodicScheduledCampaign fromDto(SchedulingCampaignDto dto) {
        return new PeriodicScheduledCampaign(Long.valueOf(dto.id), dto.campaignsId, dto.campaignsTitle, dto.schedulingDate, toFrequency(dto.frequency));
    }

    private SchedulingCampaignDto toDto(long id, PeriodicScheduledCampaign periodicScheduledCampaign) {
        return new SchedulingCampaignDto(String.valueOf(id), periodicScheduledCampaign.campaignsId, periodicScheduledCampaign.campaignsTitle, periodicScheduledCampaign.nextExecutionDate, periodicScheduledCampaign.frequency.label);
    }
}
