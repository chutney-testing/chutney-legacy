package com.chutneytesting.task.assertion;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;

public class XsdValidationTask implements Task {

	private String xml;
	private String xsdPath;
	private Logger logger;

	public XsdValidationTask(Logger logger, @Input("xml") String xml, @Input("xsd") String xsdPath) {
		this.logger = logger;
		this.xml = xml;
		this.xsdPath = xsdPath;
	}

	@Override
	public TaskExecutionResult execute() {

		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			String fileUrl = XsdValidationTask.class.getResource(xsdPath).getFile();
			Schema schema = factory.newSchema(new File(fileUrl));
			Validator validator = schema.newValidator();
			try(StringReader sr = new StringReader(xml)) {
                StreamSource ss = new StreamSource(sr);
                validator.validate(ss);
            }
		} catch (SAXException | IOException e) {
			logger.error("Exception: " + e.getMessage());
			return TaskExecutionResult.ko();
		}
		return TaskExecutionResult.ok();
	}

}
