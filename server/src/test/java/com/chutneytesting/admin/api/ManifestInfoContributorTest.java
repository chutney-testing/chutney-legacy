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

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

public class ManifestInfoContributorTest {

    @Test
    public void read_basic_manifest() {
        Info.Builder infoBuilder = new Info.Builder();

        InfoContributor manifestInfoContributor = new ManifestInfoContributor();

        manifestInfoContributor.contribute(infoBuilder);

        Info info = infoBuilder.build();

        Assertions.assertThat(info.getDetails())
            .as("Manifest Info")
            .containsEntry("chutney-test-manifest", ImmutableMap.builder()
                .put("Manifest-Version", "1.0")
                .put("Implementation-Title", "chutney-test-manifest")
                .put("Implementation-Version", "1.0.0-SNAPSHOT")
                .put("Built-By", "toto ;)")
                .put("Specification-Title", "server")
                .put("Implementation-Vendor-Id", "com.chutneytesting")
                .put("Created-By", "Apache Maven 3.2.2")
                .put("Build-Jdk", "1.8.0_121")
                .put("Specification-Version", "1.0")
                .build()
            )
        ;
    }
}
