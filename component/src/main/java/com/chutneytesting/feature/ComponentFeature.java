package com.chutneytesting.feature;

import com.chutneytesting.server.core.domain.feature.Feature;
import org.springframework.stereotype.Component;

@Component
public class ComponentFeature implements Feature {

    @Override
    public String name() {
        return "COMPONENT";
    }

}
