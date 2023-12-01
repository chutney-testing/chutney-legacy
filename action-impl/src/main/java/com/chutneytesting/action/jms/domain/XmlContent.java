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

package com.chutneytesting.action.jms.domain;

import com.google.common.base.Ascii;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlContent {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlContent.class);

    private static final String XML_DEFAULT_NAMESPACE_REGEX = "xmlns=\"[^\"]+\"";
    private static final Pattern XML_DEFAULT_NAMESPACE_PATTERN = Pattern.compile(XML_DEFAULT_NAMESPACE_REGEX);

    private static final String XML_NAMESPACED_TAG_REGEX = "<(?<end>/?)([^:>\\s]+):(?<tag>[^>\\s]+)";
    private static final Pattern XML_NAMESPACED_TAG_PATTERN = Pattern.compile(XML_NAMESPACED_TAG_REGEX);

    private final SAXBuilder saxBuilder;
    private final String stringRepresentationWithoutNamespaces;

    public XmlContent(SAXBuilder saxBuilder, String stringRepresentation) {
        this.saxBuilder = saxBuilder;

        Matcher matcher = XML_DEFAULT_NAMESPACE_PATTERN.matcher(stringRepresentation);
        String stringRepresentationWithoutDefaultNamespace = matcher.replaceAll("");

        this.stringRepresentationWithoutNamespaces = removeTagNamespaces(stringRepresentationWithoutDefaultNamespace);
    }

    public Optional<Document> tryBuildDocumentWithoutNamespaces() {
        Optional<Document> document;
        try {
            document = Optional.of(buildDocumentWithoutNamespaces());
        } catch (InvalidXmlDocumentException e) {
            LOGGER.warn(e.getMessage(), e);
            document = Optional.empty();
        }
        return document;
    }

    public Document buildDocumentWithoutNamespaces() throws InvalidXmlDocumentException {

        try {
            return saxBuilder.build(new ByteArrayInputStream(stringRepresentationWithoutNamespaces.getBytes()));
        } catch (JDOMException | IOException e) {
            throw new InvalidXmlDocumentException(stringRepresentationWithoutNamespaces, e);
        }
    }

    private String removeTagNamespaces(String stringRepresentationWithoutDefaultNamespace) {
        final StringBuilder sb = new StringBuilder();
        Matcher matcher1 = XML_NAMESPACED_TAG_PATTERN.matcher(stringRepresentationWithoutDefaultNamespace);
        while (matcher1.find()) {
            String end = matcher1.group("end");
            String tag = matcher1.group("tag");

            matcher1.appendReplacement(sb, "<" +
                (end.isEmpty() ? "" : "/") +
                tag
            );
        }
        matcher1.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("serial")
    public static class InvalidXmlDocumentException extends RuntimeException {
        InvalidXmlDocumentException(String documentAsString, Exception cause) {
            super("Unable to parse XML: " + Ascii.truncate(documentAsString, 100, "..."), cause);
        }
    }
}
