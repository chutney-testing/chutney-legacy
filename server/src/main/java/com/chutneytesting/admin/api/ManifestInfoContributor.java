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

package com.chutneytesting.admin.api;


import static com.chutneytesting.tools.loader.ExtensionLoaders.Sources.classpath;

import com.chutneytesting.tools.loader.ExtensionLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ManifestInfoContributor implements InfoContributor {
    @Override
    public void contribute(Builder builder) {
        ExtensionLoader.Builder
            .<String, String>withSource(classpath("META-INF/MANIFEST.MF"))
            .withMapper(Collections::singleton)
        .load()
        .stream()
        .map(ManifestInfoContributor::toMap)
        .filter(manifestMap -> manifestMap.getOrDefault("Implementation-Title", "").contains("chutney"))
        .forEach(manifestMap -> builder.withDetail(manifestMap.get("Implementation-Title"), manifestMap));
    }

    static Map<String, String> toMap(String manifestAsString) {
        Manifest manifest;
        try {
            manifest = new Manifest(new ByteArrayInputStream(manifestAsString.getBytes()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read Manifest:\n" + manifestAsString);
        }

        return manifest.getMainAttributes()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                entry -> String.valueOf(entry.getValue())
            ));
    }
}
