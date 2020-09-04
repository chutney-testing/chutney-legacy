package com.chutneytesting.design.infra.storage.jira;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.design.domain.jira.JiraConfigurationNotFoundException;
import com.chutneytesting.design.domain.jira.JiraRepository;
import com.chutneytesting.design.domain.jira.JiraTargetConfiguration;
import com.chutneytesting.tools.ZipUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JiraFileRepository implements JiraRepository {

    private static final String FILE_EXTENSION = ".json";
    private static final String SCENARIO_FILE = "scenario_link" + FILE_EXTENSION;
    private static final String CAMPAIGN_FILE = "campaign_link" + FILE_EXTENSION;
    private static final String CONFIGURATION_FILE = "server" + FILE_EXTENSION;

    static final Path ROOT_DIRECTORY_NAME = Paths.get("jira");

    private final Path storeFolderPath;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    JiraFileRepository(@Value("${configuration-folder:conf}") String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        initFolder(this.storeFolderPath);
    }

    @Override
    public void backup(OutputStream outputStream) throws UncheckedIOException {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            Path jiraDirectoryPath = this.storeFolderPath;
            ZipUtils.compressDirectoryToZipfile(jiraDirectoryPath.getParent(), jiraDirectoryPath.getFileName(), zipOutPut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getByScenarioId(String scenarioId) {
        String scenarioIdFormatted = scenarioId.startsWith("#") ? scenarioId : "#" + scenarioId.replace("-", ":");
        return getById(SCENARIO_FILE, scenarioIdFormatted);
    }

    @Override
    public void saveForScenario(String scenarioFrontId, String jiraId) {
        String scenarioId = "#" + scenarioFrontId.replace("-", ":");
        save(SCENARIO_FILE, scenarioId, jiraId);
    }

    @Override
    public String getByCampaignId(String campaignId) {
        return getById(CAMPAIGN_FILE, campaignId);
    }

    @Override
    public void saveForCampaign(String campaignId, String jiraId) {
        save(CAMPAIGN_FILE, campaignId, jiraId);
    }

    @Override
    public JiraTargetConfiguration loadServerConfiguration() {
        return doLoadServerConfiguration();
    }

    @Override
    public void saveServerConfiguration(JiraTargetConfiguration jiraTargetConfiguration) {
        Path resolvedFilePath = storeFolderPath.resolve(CONFIGURATION_FILE);
        doSave(resolvedFilePath, jiraTargetConfiguration);
    }

    private JiraTargetConfiguration doLoadServerConfiguration() {
        Path configurationFilePath = storeFolderPath.resolve(CONFIGURATION_FILE);
        if (!Files.exists(configurationFilePath)) {
            throw new JiraConfigurationNotFoundException("Configuration file not found: " + configurationFilePath);
        }
        try {
            byte[] bytes = Files.readAllBytes(configurationFilePath);
            try {
                return objectMapper.readValue(bytes, JiraTargetConfiguration.class);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot deserialize configuration file: " + configurationFilePath, e);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + configurationFilePath, e);
        }
    }

    private String getById(String filePath, String id) {
        Path resolvedFilePath = storeFolderPath.resolve(filePath);
        if (!Files.exists(resolvedFilePath)) {
            return "";
        }
        try {
            byte[] bytes = Files.readAllBytes(resolvedFilePath);
            try {
                Map<String, String> map = objectMapper.readValue(bytes, Map.class);
                return map.getOrDefault(id, "");
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot deserialize configuration file: " + resolvedFilePath, e);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + resolvedFilePath, e);
        }
    }

    private void save(String filePath, String chutneyId, String jiraId) {
        Path resolvedFilePath = storeFolderPath.resolve(filePath);
        try {
            Map<String, String> map = new HashMap<>();

            if (Files.exists(resolvedFilePath)) {
                byte[] bytes = Files.readAllBytes(resolvedFilePath);
                map.putAll(objectMapper.readValue(bytes, Map.class));
            }

            map.put(chutneyId, jiraId);
            doSave(resolvedFilePath, map);

        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + resolvedFilePath, e);
        }
    }

    private void doSave(Path path, Object map) {

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(map);
            try {
                Files.write(path, bytes);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot toNode in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + map, e);
        }
    }

}
