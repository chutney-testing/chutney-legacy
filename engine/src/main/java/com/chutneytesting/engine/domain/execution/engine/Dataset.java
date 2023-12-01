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

package com.chutneytesting.engine.domain.execution.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Dataset {

    public final Map<String, String> constants;
    public final List<Map<String, String>> datatable;

    public Dataset() {
        this(Collections.emptyMap(), Collections.emptyList());
    }

    public Dataset(Map<String, String> constants, List<Map<String, String>> datatable) {
        this.constants = Collections.unmodifiableMap(constants);
        this.datatable = Collections.unmodifiableList(datatable);
    }

}
