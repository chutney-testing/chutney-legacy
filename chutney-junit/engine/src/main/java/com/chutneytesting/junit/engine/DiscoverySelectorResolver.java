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

package com.chutneytesting.junit.engine;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.junit.api.Chutney;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.UriSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class DiscoverySelectorResolver {

    public static final String FEATURE_SEGMENT_TYPE = "feature";
    public static final String SCENARIO_SEGMENT_TYPE = "scenario";

    private static final String FEATURE_EXTENSION = ".feature";

    private final PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
    private final GlacioAdapter glacioAdapter;
    private boolean classMode;
    private final String environmentName;

    public DiscoverySelectorResolver(GlacioAdapter glacioAdapter, String environmentName) {
        this.glacioAdapter = glacioAdapter;
        this.environmentName = environmentName;
    }

    public void resolveSelectors(EngineDiscoveryRequest engineDiscoveryRequest, ChutneyEngineDescriptor engineDescriptor) {
        Predicate<String> packageNameFilter = Filter.composeFilters(engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class)).toPredicate();
        Predicate<String> classNameFilter = Filter.composeFilters(engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class)).toPredicate();

        // Keep class selector first in line in order to position classMode property
        List<ClassSelector> classSelectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        classSelectors.forEach(cs -> resolveClass(engineDescriptor, cs.getJavaClass()));

        List<FileSelector> fileSelectors = engineDiscoveryRequest.getSelectorsByType(FileSelector.class);
        fileSelectors.forEach(fs -> resolveFile(engineDescriptor, fs.getFile()));

        List<DirectorySelector> directorySelectors = engineDiscoveryRequest.getSelectorsByType(DirectorySelector.class);
        directorySelectors.forEach(ds -> resolveDirectory(engineDescriptor, ds.getDirectory()));

        List<ClasspathRootSelector> classpathRootSelectors = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        classpathRootSelectors.forEach(crs -> resolveClassPathRoot(engineDescriptor, crs.getClasspathRoot(), packageNameFilter.and(classNameFilter)));

        List<PackageSelector> packageSelectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        packageSelectors.stream()
            .filter(ps -> packageNameFilter.test(ps.getPackageName()))
            .forEach(ps -> resolvePackage(engineDescriptor, ps.getPackageName()));

        List<ClasspathResourceSelector> classpathResourceSelectors = engineDiscoveryRequest.getSelectorsByType(ClasspathResourceSelector.class);
        classpathResourceSelectors.stream()
            .filter(crs -> packageNameFilter.test(crs.getClasspathResourceName().replace("/", ".")))
            .forEach(crs -> resolveClassPathResource(engineDescriptor, crs.getClasspathResourceName()));

        List<UriSelector> uriSelectors = engineDiscoveryRequest.getSelectorsByType(UriSelector.class);
        uriSelectors.forEach(us -> resolveURI(engineDescriptor, us.getUri()));

        // Use UniqueId selectors as filter over current engine descriptor. As such, keep this last in line.
        List<UniqueIdSelector> uniqueIdSelectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        resolveUniqueIds(engineDescriptor, uniqueIdSelectors.stream().map(UniqueIdSelector::getUniqueId).collect(toList()));
    }

    private void resolveUniqueIds(ChutneyEngineDescriptor engineDescriptor, List<UniqueId> uniqueIds) {
        if (uniqueIds.isEmpty()) {
            return;
        }

        if (engineDescriptor.getChildren().isEmpty()) {
            resolvePackage(engineDescriptor, "");
        }

        List<String> uniqueIdsStrings = uniqueIds.stream().map(UniqueId::toString).toList();
        List<? extends TestDescriptor> testDescriptors = engineDescriptor.getChildren().stream()
            .flatMap(td -> td.getChildren().stream())
            .filter(ts -> uniqueIdsStrings.stream().noneMatch(uis -> ts.getUniqueId().toString().contains(uis)))
            .toList();

        testDescriptors.forEach(TestDescriptor::removeFromHierarchy);

        List<? extends TestDescriptor> emptyFeatures = engineDescriptor.getChildren().stream().filter(ts -> ts.getChildren().isEmpty()).toList();
        emptyFeatures.forEach(TestDescriptor::removeFromHierarchy);
    }

    private void resolveURI(TestDescriptor parent, URI uri) {
        resolveResource(parent, pathResolver.getResource(uri.toString()));
    }

    private void resolveClassPathResource(TestDescriptor parent, String classPathResourceName) {
        if (hasFeatureExtension(classPathResourceName)) {
            Resource resource = pathResolver.getResource("classpath:" + classPathResourceName);
            resolveResource(parent, resource);
        }
    }

    private void resolveClass(TestDescriptor parent, Class<?> aClass) {
        if (aClass.isAnnotationPresent(Chutney.class)) {
            classMode = true;
            String classPackageName = aClass.getPackage().getName();
            resolvePackage(parent, classPackageName.replace(".", "/"));
        }
    }

    private void resolvePackage(TestDescriptor parent, String packageName) {
        try {
            Resource[] resources = pathResolver.getResources("classpath*:" + packageName.replace(".", "/") + "/**/*" + FEATURE_EXTENSION);
            for (Resource resource : resources) {
                resolveResource(parent, resource);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException("Cannot get resources from " + packageName, ioe);
        }
    }

    private void resolveClassPathRoot(TestDescriptor parent, URI classpathRoot, Predicate<String> classFilter) {
        try {
            Resource[] resources = pathResolver.getResources(classpathRoot.toString() + "/**/*" + FEATURE_EXTENSION);
            for (Resource resource : resources) {
                String pathFromRoot = resource.getURI().getPath().replace(classpathRoot.getPath(), "").replace("/", ".");
                if (classFilter.test(pathFromRoot)) {
                    resolveResource(parent, resource);
                }
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException("Cannot get resources from " + classpathRoot, ioe);
        }
    }

    private void resolveDirectory(TestDescriptor parent, File dir) {
        if (dir.exists() && dir.isDirectory()) {
            try {
                List<Path> features = Files.walk(dir.toPath())
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> hasFeatureExtension(path.toFile().getName()))
                    .toList();

                features.forEach(path -> resolveFile(parent, path.toFile()));
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
    }

    private void resolveFile(TestDescriptor parent, File file) {
        if (file.exists() && file.isFile()) {
            resolveFeature(parent, file.getName(), content(file), UriSource.from(file.toURI()));
        }
    }

    private void resolveResource(TestDescriptor parent, Resource resource) {
        if (resource.exists()) {
            try {
                resolveFeature(parent, resourceName(resource), content(resource.getInputStream()), UriSource.from(resource.getURI()));
            } catch (IOException ioe) {
                throw new UncheckedIOException("Cannot get inputstream from " + resource.getDescription(), ioe);
            }
        }
    }

    private void resolveFeature(TestDescriptor parent, String name, String featureContent, TestSource testSource) {
        UniqueId uniqueId = parent.getUniqueId().append(FEATURE_SEGMENT_TYPE, name);
        if (parent.findByUniqueId(uniqueId).isEmpty()) {

            FeatureDescriptor featureDescriptor = new FeatureDescriptor(uniqueId, name, featureSource(testSource));

            List<StepDefinitionDto> stepDefinitions = parseFeature(featureContent);
            stepDefinitions.forEach(stepDefinition -> resolveScenario(featureDescriptor, stepDefinition));

            parent.addChild(featureDescriptor);
        }
    }

    private void resolveScenario(FeatureDescriptor parentFeature, StepDefinitionDto stepDefinition) {
        ScenarioDescriptor scenarioDescriptor =
            new ScenarioDescriptor(
                parentFeature.getUniqueId().append(SCENARIO_SEGMENT_TYPE, stepDefinition.name),
                stepDefinition.name,
                null,
                stepDefinition);

        parentFeature.addChild(scenarioDescriptor);
    }

    private List<StepDefinitionDto> parseFeature(String featureContent) {
        return glacioAdapter.toChutneyStepDefinition(featureContent, environmentName);
    }

    private String content(File file) {
        try {
            return content(new FileInputStream(file));
        } catch (FileNotFoundException fnfe) {
            throw new UncheckedIOException("Unable to read " + file.getAbsolutePath(), fnfe);
        }
    }

    private String content(InputStream in) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        } catch (IOException ioe) {
            throw new UncheckedIOException("Unable to read inputstream", ioe);
        }
    }

    private String resourceName(Resource resource) {
        try {
            String[] split = resource.getURI().getSchemeSpecificPart().split("/");
            if (split.length > 0) {
                return split[split.length - 1];
            }
            return resource.toString();
        } catch (IOException ioe) {
            throw new UncheckedIOException("Cannot get URI from " + resource.getDescription(), ioe);
        }
    }

    private boolean hasFeatureExtension(String name) {
        return name.endsWith(FEATURE_EXTENSION);
    }

    private TestSource featureSource(TestSource testSource) {
        if (classMode) {
            String name = testSource.toString();
            if (testSource instanceof UriSource uriSource) {
                name = uriSource.getUri().getSchemeSpecificPart();
            }
            return ClassSource.from(name);
        }
        return testSource;
    }
}
