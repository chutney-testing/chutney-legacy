package com.chutneytesting.execution.api.report.surefire;

import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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

    public byte[] createReport(List<CampaignExecutionReport> campaignExecutionReports) {
        List<CampaignReportFolder> campaignReportFolders = campaignExecutionReports.stream()
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

    private CampaignReportFolder createReport(CampaignExecutionReport campaignExecutionReport) {
        return new CampaignReportFolder(campaignExecutionReport.campaignName, testsuite(campaignExecutionReport.scenarioExecutionReports()));
    }

    private Set<Testsuite> testsuite(List<ScenarioExecutionReportCampaign> scenarioExecutionHistory) {
        return scenarioExecutionHistory
            .stream()
            .map(surefireScenarioExecutionReportBuilder::create)
            .collect(Collectors.toSet());
    }
}
