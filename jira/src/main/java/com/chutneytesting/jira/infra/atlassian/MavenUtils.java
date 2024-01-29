/*
 * Copyright (C) 2012 Atlassian
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
package com.chutneytesting.jira.infra.atlassian;

import java.io.InputStream;
import java.util.Properties;

final class MavenUtils {
    private static final String UNKNOWN_VERSION = "unknown";

    static String getVersion(String groupId, String artifactId) {
        final Properties props = new Properties();
        try (InputStream resourceAsStream = artifactPomAsStream(groupId, artifactId)) {
            props.load(resourceAsStream);
            return props.getProperty("version", UNKNOWN_VERSION);
        } catch (Exception e) {
            return UNKNOWN_VERSION;
        }
    }

    private static InputStream artifactPomAsStream(String groupId, String artifactId) {
        return MavenUtils.class.getResourceAsStream(
            String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId)
        );
    }
}
