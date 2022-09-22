package com.chutneytesting.task.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.TestLogger;

public class XsdValidationTaskTest {

    XsdValidationTask task;

    @Test
    public void should_validate_simple_xsd() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\"?>\r\n" +
            "<Employee xmlns=\"https://www.chutneytesting.com/Employee\">\r\n" +
            "    <name>my Name</name>\r\n" +
            "    <age>29</age>\r\n" +
            "    <role>Java Developer</role>\r\n" +
            "    <gender>Male</gender>\r\n" +
            "</Employee>";

        String xsd = "/xsd_samples/employee.xsd";

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);
    }


    @Test
    public void should_not_validate_simple_xsd() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\"?>\r\n" +
            "<Employee xmlns=\"https://www.chutneytesting.com/Employee\">\r\n" +
            "    <name>my name</name>\r\n" +
            "    <age>29</age>\r\n" +
            "    <role>Java Developer</role>\r\n" +
            "    <gender>ERROR</gender>\r\n" +
            "</Employee>";

        String xsd = "/xsd_samples/employee.xsd";

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Failure);
    }

    @Test
    public void should_validate_xsd_with_multiple_import() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<shipTo xmlns=\"http://chutney/test/ship\"\n" +
            "        xmlns:addr=\"http://chutney/test/address\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://chutney/test/ship shipTo.xsd\">\n" +
            "    <name>string</name>\n" +
            "    <address>\n" +
            "        <addr:street>voltaire</addr:street>\n" +
            "        <addr:type>Rue</addr:type>\n" +
            "        <addr:city>Paris</addr:city>\n" +
            "        <addr:country>France</addr:country>\n" +
            "    </address>\n" +
            "</shipTo>\n";

        String xsd = "/xsd_samples/shipTo.xsd";

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);


    }

    @Test
    public void should_validate_xsd_from_classpath() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<shipTo xmlns=\"http://chutney/test/ship\"\n" +
            "        xmlns:addr=\"http://chutney/test/address\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://chutney/test/ship shipTo.xsd\">\n" +
            "    <name>string</name>\n" +
            "    <address>\n" +
            "        <addr:street>voltaire</addr:street>\n" +
            "        <addr:type>Rue</addr:type>\n" +
            "        <addr:city>Paris</addr:city>\n" +
            "        <addr:country>France</addr:country>\n" +
            "    </address>\n" +
            "</shipTo>";

        String xsd = "classpath:/xsd_samples/shipTo.xsd";

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);


    }

    @Test
    public void should_validate_xsd_from_file_system() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<shipTo xmlns=\"http://chutney/test/ship\"\n" +
            "        xmlns:addr=\"http://chutney/test/address\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://chutney/test/ship shipTo.xsd\">\n" +
            "    <name>string</name>\n" +
            "    <address>\n" +
            "        <addr:street>voltaire</addr:street>\n" +
            "        <addr:type>Rue</addr:type>\n" +
            "        <addr:city>Paris</addr:city>\n" +
            "        <addr:country>France</addr:country>\n" +
            "    </address>\n" +
            "</shipTo>";

        Path executionPath = Paths.get("").toAbsolutePath();
        String xsd = "file:" + executionPath.resolve("src/test/resources/xsd_samples/shipTo.xsd");

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);


    }

    @Test
    public void should_validate_xsd_from_jar_in_classpath() {
        Logger logger = new TestLogger();
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<shipTo xmlns=\"http://chutney/test/ship\"\n" +
            "        xmlns:addr=\"http://chutney/test/address\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://chutney/test/ship shipTo.xsd\">\n" +
            "    <name>string</name>\n" +
            "    <address>\n" +
            "        <addr:street>voltaire</addr:street>\n" +
            "        <addr:type>Rue</addr:type>\n" +
            "        <addr:city>Paris</addr:city>\n" +
            "        <addr:country>France</addr:country>\n" +
            "    </address>\n" +
            "</shipTo>";

        String xsd = "/xsd/shipTo.xsd";

        task = new XsdValidationTask(logger, xml, xsd);

        //When
        TaskExecutionResult result = task.execute();

        //Then
        assertThat(result.status).isEqualTo(Status.Success);

    }
}
