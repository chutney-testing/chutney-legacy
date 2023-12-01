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

package com.chutneytesting.feature.api;

import com.chutneytesting.feature.api.dto.FeatureDto;
import com.chutneytesting.server.core.domain.feature.Feature;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/features")
@CrossOrigin(origins = "*")
public class FeatureController {

    private final List<Feature> features;

    public FeatureController(List<Feature> features) {
        this.features = features;
    }

    @GetMapping
    public List<FeatureDto> getAll() {
        return features.stream().map(feature -> new FeatureDto(feature.name(), feature.active())).toList();
    }
}
