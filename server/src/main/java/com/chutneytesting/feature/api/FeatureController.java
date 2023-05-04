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
