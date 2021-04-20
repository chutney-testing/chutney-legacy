package com.chutneytesting.design.infra.storage.plugins.jira;

import static com.chutneytesting.tools.file.FileUtils.initFolder;
import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;

import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.plugins.jira.JiraTargetConfiguration;
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
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JiraFileRepository implements JiraRepository {

    private static final String FILE_EXTENSION = ".json";
    private static final String SCENARIO_FILE = "scenario_link" + FILE_EXTENSION;
    private static final String CAMPAIGN_FILE = "campaign_link" + FILE_EXTENSION;
    private static final String CONFIGURATION_FILE = "jira_config" + FILE_EXTENSION;

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("jira");

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
            ZipUtils.compressDirectoryToZipfile(storeFolderPath.getParent(), storeFolderPath.getFileName(), zipOutPut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Map<String, String> getAllLinkedCampaigns() {
        return getAll(CAMPAIGN_FILE);
    }

    @Override
    public Map<String, String> getAllLinkedScenarios() {
        return getAll(SCENARIO_FILE)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> toFrontId(entry.getKey()), entry -> entry.getValue()));
    }

    @Override
    public String getByScenarioId(String scenarioId) {
        String scenarioIdFormatted = fromFrontId(scenarioId);
        return getById(SCENARIO_FILE, scenarioIdFormatted);
    }

    @Override
    public void saveForScenario(String scenarioFrontId, String jiraId) {
        String scenarioId = fromFrontId(scenarioFrontId);
        save(SCENARIO_FILE, scenarioId, jiraId);
    }

    @Override
    public void removeForScenario(String scenarioId) {
        remove(SCENARIO_FILE, fromFrontId(scenarioId));
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
    public void removeForCampaign(String campaignId) {
        remove(CAMPAIGN_FILE, campaignId);
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
            return new JiraTargetConfiguration("", "", "");
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
        return getAll(filePath).getOrDefault(id, "");
    }

    private Map<String, String> getAll(String filePath) {
        Path resolvedFilePath = storeFolderPath.resolve(filePath);
        if (!Files.exists(resolvedFilePath)) {
            return new HashMap<>();
        }
        try {
            byte[] bytes = Files.readAllBytes(resolvedFilePath);
            try {
                return objectMapper.readValue(bytes, Map.class);
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

            if (jiraId.isEmpty())
                map.remove(chutneyId);
            else
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
                throw new UnsupportedOperationException("Cannot write in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + map, e);
        }
    }

    private void remove(String filePath, String chutneyId) {
        Path resolvedFilePath = storeFolderPath.resolve(filePath);
        try {
            Map<String, String> map = new HashMap<>();

            if (Files.exists(resolvedFilePath)) {
                byte[] bytes = Files.readAllBytes(resolvedFilePath);
                map.putAll(objectMapper.readValue(bytes, Map.class));
            }

            map.remove(chutneyId);
            doSave(resolvedFilePath, map);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + resolvedFilePath, e);
        }
    }

}
