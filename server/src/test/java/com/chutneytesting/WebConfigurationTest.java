package com.chutneytesting;


import static org.assertj.core.api.Assertions.assertThatCode;

import com.chutneytesting.action.common.XmlUtils;
import com.chutneytesting.action.function.XPathFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

class WebConfigurationTest {

    private static final String STANDARD_XML =
            "<node1>\n" +
            "    <node2 attr1=\"val4\"/>\n" +
            "</node1>";


    @Test
    void verify_no_infinite_recursion_when_serializing_jdom2_element() throws XmlUtils.InvalidXmlDocumentException, XmlUtils.InvalidXPathException, JsonProcessingException {
        WebConfiguration webConfiguration = new WebConfiguration();
        ObjectMapper reportObjectMapper = webConfiguration.reportObjectMapper();

        Element result = (Element) XPathFunction.xpath(STANDARD_XML, "/node1/node2");

        assertThatCode(() -> reportObjectMapper.writeValueAsString(result)).doesNotThrowAnyException();
    }
}
