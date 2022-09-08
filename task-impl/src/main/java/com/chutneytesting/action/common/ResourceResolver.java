package com.chutneytesting.action.common;

import com.chutneytesting.action.assertion.XsdValidationAction;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ResourceResolver implements LSResourceResolver {
    private final String urlPrefix;
    private final Path rootResourcePath;
    private List<String> urlPrefixes = Arrays.asList(
        ResourceUtils.CLASSPATH_URL_PREFIX,
        ResourceUtils.FILE_URL_PREFIX);

    public ResourceResolver(String rootFilePath) {
        urlPrefix = urlPrefixes.stream().filter(rootFilePath::startsWith)
            .findFirst()
            .orElse(ResourceUtils.CLASSPATH_URL_PREFIX);
        rootResourcePath = Path.of(rootFilePath.replaceFirst(urlPrefix, "")).getParent();
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)  {

        try {
            Path xsdPath = rootResourcePath.resolve(systemId);
            if (StringUtils.isNotEmpty(baseURI)) {
                xsdPath = rootResourcePath.resolve(getParentResourcePath(baseURI)).resolve(systemId);
            }
            ResourceLoader resourceLoader = new DefaultResourceLoader(XsdValidationAction.class.getClassLoader());
            Resource resource = resourceLoader.getResource(urlPrefix + xsdPath);
            LSInputImpl input = new LSInputImpl();
            input.setPublicId(publicId);
            input.setSystemId(systemId);
            input.setBaseURI(baseURI);
            input.setByteStream(resource.getInputStream());
            return input;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private Path getParentResourcePath(String baseURI) {
        return Path.of(StringUtils.substringAfter(baseURI,"/chutney/task-impl/")).getParent(); // TODO - lessen tight coupling to mvn module name
    }
}
