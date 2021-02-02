package com.chutneytesting.task.jms.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Scanner;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.jupiter.api.Test;

public class XmlContentTest {

    @Test
    public void xpath_works_whereas_default_namespace() {
        XmlContent xmlContent = new XmlContent(new SAXBuilder(), loadFileFromClasspath("xml_samples/with_default_and_tag_namespaces.xml"));

        XPathExpression<Text> xPathExpression = XPathFactory.instance().compile("/descriptionComplete/evenementNatureCode/text()", Filters.text());
        String result = xPathExpression.evaluateFirst(xmlContent.tryBuildDocumentWithoutNamespaces().get()).getText();
        assertThat(result).isEqualTo("TEST_CODE");

        xPathExpression = XPathFactory.instance().compile("/descriptionComplete/test1/test2/number/text()", Filters.text());
        result = xPathExpression.evaluateFirst(xmlContent.tryBuildDocumentWithoutNamespaces().get()).getText();
        assertThat(result).isEqualTo("5072899");
    }

    @Test
    public void xpath_works_whereas_local_tag_namespace() {
        XmlContent xmlContent = new XmlContent(new SAXBuilder(), loadFileFromClasspath("xml_samples/with_default_and_tag_namespaces.xml"));

        XPathExpression<Text> xPathExpression = XPathFactory.instance().compile("/descriptionComplete/test1/test2/number/text()", Filters.text());
        String result = xPathExpression.evaluateFirst(xmlContent.tryBuildDocumentWithoutNamespaces().get()).getText();
        assertThat(result).isEqualTo("5072899");
    }

    @Test
    public void buildDocument_of_unparsable_xl_returns_empty() {
        XmlContent xmlContent = new XmlContent(new SAXBuilder(), "<test");

        assertThat(xmlContent.tryBuildDocumentWithoutNamespaces()).isEmpty();
    }

    @SuppressWarnings("resource")
    private String loadFileFromClasspath(String filePath) {
        return new Scanner(XmlContentTest.class.getClassLoader().getResourceAsStream(filePath), "UTF-8").useDelimiter("\\A").next();
    }
}
