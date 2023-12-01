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

package util.infra;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(
    basePackages = {
        "com.chutneytesting.scenario.infra",
        "com.chutneytesting.campaign.infra",
        "com.chutneytesting.execution.infra.storage"
    },
    includeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.*JpaRepository$")}
)
@ComponentScan(
    basePackages = {
        "com.chutneytesting.campaign.infra",
        "com.chutneytesting.scenario.infra",
        "com.chutneytesting.execution.infra.storage"
    }
)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableJpa {
}
