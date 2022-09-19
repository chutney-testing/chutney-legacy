package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.common.ResourceResolver;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.SAXException;

public class XsdValidationTask implements Task {

    private String xml;
    private String xsdPath;
    private Logger logger;
    private ResourceLoader resourceLoader = new DefaultResourceLoader(XsdValidationTask.class.getClassLoader());

    public XsdValidationTask(Logger logger, @Input("xml") String xml, @Input("xsd") String xsdPath) {
        this.logger = logger;
        this.xml = xml;
        this.xsdPath = xsdPath;
    }

    @Override
    public List<String> validateInputs() {
        Validator<String> xmlValidation = of(xsdPath)
            .validate(Objects::nonNull, "No xsd provided")
            .validate(x -> resourceLoader.getResource(x), resource -> resource.exists(), "Cannot find xsd");
        return getErrorsFrom(xmlValidation, notBlankStringValidation(xml, "xml"));
    }

    @Override
    public TaskExecutionResult execute() {
        try {

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(new ResourceResolver(xsdPath));

            Resource resource = resourceLoader.getResource(xsdPath);
            Source schemaSource = new StreamSource(resource.getInputStream());
            Schema schema = factory.newSchema(schemaSource);
            javax.xml.validation.Validator validator = schema.newValidator();
            try (StringReader sr = new StringReader(xml)) {
                StreamSource ss = new StreamSource(sr);
                validator.validate(ss);
            }
        } catch (SAXException | IOException | UncheckedIOException e ) {
            logger.error("Exception: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
        return TaskExecutionResult.ok();
    }

}
