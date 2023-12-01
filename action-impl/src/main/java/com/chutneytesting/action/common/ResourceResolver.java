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

package com.chutneytesting.action.common;

import com.chutneytesting.action.assertion.XsdValidationAction;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private final List<String> urlPrefixes = Arrays.asList(
        ResourceUtils.CLASSPATH_URL_PREFIX,
        ResourceUtils.FILE_URL_PREFIX);

    public ResourceResolver(String rootFilePath) {
        urlPrefix = urlPrefixes.stream().filter(rootFilePath::startsWith)
            .findFirst()
            .orElse(ResourceUtils.CLASSPATH_URL_PREFIX);
        rootResourcePath = Path.of(rootFilePath.replaceFirst(urlPrefix, "")).getParent();
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

        try {
            Path xsdPath = resolveBaseResourcePath(baseURI).resolve(systemId);
            ResourceLoader resourceLoader = new DefaultResourceLoader(XsdValidationAction.class.getClassLoader());
            Resource resource = resourceLoader.getResource(urlPrefix + xsdPath);
            LSInputImpl input = new LSInputImpl();
            input.setPublicId(publicId);
            input.setSystemId(systemId);
            input.setBaseURI(fixBaseURI(baseURI));
            input.setByteStream(resource.getInputStream());
            return input;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolveBaseResourcePath(String baseURI) {
        Path path = rootResourcePath;
        if (StringUtils.isNotEmpty(baseURI)) {
            try {
                path = Path.of(new URI(baseURI));
                if (urlPrefix.equals(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                    path = Path.of(System.getProperty("user.dir")).relativize(path);
                }
                path = rootResourcePath.resolve(Optional.ofNullable(path.getParent()).orElse(path));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not create URI from " + baseURI, e);
            }
        }
        return path;
    }

    private String fixBaseURI(String baseURI) {
        if (urlPrefix.equals(ResourceUtils.CLASSPATH_URL_PREFIX) || StringUtils.isNotEmpty(baseURI)) {
            return baseURI;
        }
        return rootResourcePath.toUri().toString();
    }
}
