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

package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.common.ResourceResolver;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
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

public class XsdValidationAction implements Action {

    private String xml;
    private String xsdPath;
    private Logger logger;
    private ResourceLoader resourceLoader = new DefaultResourceLoader(XsdValidationAction.class.getClassLoader());

    public XsdValidationAction(Logger logger, @Input("xml") String xml, @Input("xsd") String xsdPath) {
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
    public ActionExecutionResult execute() {
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
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }

}
