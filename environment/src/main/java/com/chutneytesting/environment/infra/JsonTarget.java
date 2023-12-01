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

package com.chutneytesting.environment.infra;

import com.chutneytesting.environment.domain.Target;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

@JsonDeserialize(using = TargetJsonDeserializer.class)
public class JsonTarget {

    public String name;
    public String url;
    public Map<String, String> properties;

    public JsonTarget() {
    }

    public JsonTarget(String name, String url, Map<String, String> properties) {
        this.name = name;
        this.url = url;
        this.properties = properties;
    }

    public static JsonTarget from(Target t) {
        return new JsonTarget(t.name, t.url, t.properties);
    }

    public Target toTarget(String envName) {
        return Target.builder()
            .withName(name)
            .withEnvironment(envName)
            .withUrl(url)
            .withProperties(properties)
            .build();
    }
}
