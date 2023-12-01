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

package com.chutneytesting.execution.api.report.surefire;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Surefire reports consists of a ZIP archive containing a folder by campaign.<br>
 * Each campaign folder contains one testsuite file per scenario.
 */
public class SurefireCampaignExecutionReportBuilder {

    private final SurefireScenarioExecutionReportBuilder surefireScenarioExecutionReportBuilder;

    public SurefireCampaignExecutionReportBuilder(SurefireScenarioExecutionReportBuilder surefireScenarioExecutionReportBuilder) {
        this.surefireScenarioExecutionReportBuilder = surefireScenarioExecutionReportBuilder;
    }

    public byte[] createReport(List<CampaignExecution> campaignExecutions) {
        List<CampaignReportFolder> campaignReportFolders = campaignExecutions.stream()
            .map(this::createReport)
            .collect(Collectors.toList());
        return marshall(campaignReportFolders);
    }

    private byte[] marshall(List<CampaignReportFolder> campaignReportFolders) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            campaignReportFolders.forEach(campaignReportFolder -> {
                try {
                    zos.putNextEntry(new ZipEntry(campaignReportFolder.name + "/"));
                    campaignReportFolder.scenariosReport.forEach(testsuite -> {
                        try {
                            zos.putNextEntry(new ZipEntry(campaignReportFolder.name + "/" + testsuite.name + ".xml"));
                            zos.write(zipEntry(testsuite));
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e.getMessage(), e);
                        }
                    });
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e.getMessage(), e);
                }
            });
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Cannot serialize test suite.", e);
        }
        return baos.toByteArray();
    }

    private byte[] zipEntry(Testsuite testsuite) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
            JAXBContext jc = JAXBContext.newInstance(Testsuite.class);
            Marshaller m = jc.createMarshaller();
            m.marshal(testsuite, writer);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        } catch (XMLStreamException | JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CampaignReportFolder createReport(CampaignExecution campaignExecution) {
        return new CampaignReportFolder(campaignExecution.campaignName, testsuite(campaignExecution.scenarioExecutionReports()));
    }

    private Set<Testsuite> testsuite(List<ScenarioExecutionCampaign> scenarioExecutionHistory) {
        return scenarioExecutionHistory
            .stream()
            .map(surefireScenarioExecutionReportBuilder::create)
            .collect(Collectors.toSet());
    }
}
