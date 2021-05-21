package com.chutneytesting.design.infra.storage.campaign;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.design.domain.campaign.FREQUENCY.tofrequency;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.design.domain.campaign.SchedulingCampaign;
import com.chutneytesting.design.domain.campaign.SchedulingCampaignRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Scheduling campaign persistence.
 */
@Repository
public class SchedulingCampaignFileRepository implements SchedulingCampaignRepository {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("scheduling");
    private static final String SCHEDULING_CAMPAIGNS_FILE = "schedulingCampaigns.json";

    private final Path storeFolderPath;
    private final Path resolvedFilePath;
    private final AtomicLong currentMaxId = new AtomicLong();

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    SchedulingCampaignFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        this.resolvedFilePath = this.storeFolderPath.resolve(SCHEDULING_CAMPAIGNS_FILE);
        initFolder(this.storeFolderPath);
        currentMaxId.set(this.getALl().stream().map(sm -> sm.id).max(Long::compare).orElse(0L));
    }

    @Override
    public SchedulingCampaign add(SchedulingCampaign schedulingCampaign) {
        Map<String, SchedulingCampaignDto> schedulingCampaigns = readFromDisk();
        long id = currentMaxId.incrementAndGet();
        schedulingCampaigns.put(String.valueOf(id), toDto(id, schedulingCampaign));
        writeOnDisk(resolvedFilePath, schedulingCampaigns);

        return schedulingCampaign;
    }

    @Override
    public void removeById(Long id) {
        Map<String, SchedulingCampaignDto> schedulingCampaigns = readFromDisk();
        schedulingCampaigns.remove(String.valueOf(id));
        writeOnDisk(resolvedFilePath, schedulingCampaigns);
    }

    @Override
    public List<SchedulingCampaign> getALl() {
        return readFromDisk().entrySet().stream()
            .map(e -> this.fromDto(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    private Map<String, SchedulingCampaignDto> readFromDisk() {
        Map<String, SchedulingCampaignDto> stringSchedulingCampaignDTO = new HashMap<>();
        try {
            if (Files.exists(resolvedFilePath)) {
                byte[] bytes = Files.readAllBytes(resolvedFilePath);
                stringSchedulingCampaignDTO.putAll(objectMapper.readValue(bytes, new TypeReference<HashMap<String, SchedulingCampaignDto>>() {
                }));
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + resolvedFilePath, e);
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


    private SchedulingCampaign fromDto(String id, SchedulingCampaignDto dto) {
        return new SchedulingCampaign(Long.valueOf(dto.id), dto.campaignId, dto.campaignTitle, dto.schedulingDate, tofrequency(dto.frequency));
    }

    private SchedulingCampaignDto toDto(long id, SchedulingCampaign schedulingCampaign) {
        return new SchedulingCampaignDto(String.valueOf(id), schedulingCampaign.campaignId, schedulingCampaign.campaignTitle, schedulingCampaign.getSchedulingDate(), schedulingCampaign.frequency.label);
    }


}
