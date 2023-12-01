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

package com.chutneytesting.agent.domain.configure;

import static com.chutneytesting.agent.domain.configure.ConfigurationState.EXPLORING;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.FINISHED;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.NOT_STARTED;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.WRAPING_UP;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ConfigurationStateTest {
    @Test
    public void configuration_state_transition_is_coherent() {
        assertThat(NOT_STARTED.canChangeTo(EXPLORING)).isTrue();
        assertThat(EXPLORING.canChangeTo(WRAPING_UP)).isTrue();
        assertThat(WRAPING_UP.canChangeTo(FINISHED)).isTrue();

        assertThat(NOT_STARTED.canChangeTo(WRAPING_UP)).isFalse();
        assertThat(EXPLORING.canChangeTo(EXPLORING)).isFalse();
    }
}
