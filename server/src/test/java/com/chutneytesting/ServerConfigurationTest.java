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

package com.chutneytesting;


import static org.assertj.core.api.Assertions.assertThatCode;

import com.chutneytesting.action.common.XmlUtils;
import com.chutneytesting.action.function.XPathFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

class ServerConfigurationTest {

    private static final String STANDARD_XML =
            "<node1>\n" +
            "    <node2 attr1=\"val4\"/>\n" +
            "</node1>";


    @Test
    void verify_no_infinite_recursion_when_serializing_jdom2_element() throws XmlUtils.InvalidXmlDocumentException, XmlUtils.InvalidXPathException, JsonProcessingException {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        ObjectMapper reportObjectMapper = serverConfiguration.reportObjectMapper();

        Element result = (Element) XPathFunction.xpath(STANDARD_XML, "/node1/node2");

        assertThatCode(() -> reportObjectMapper.writeValueAsString(result)).doesNotThrowAnyException();
    }
}
