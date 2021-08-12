package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.SCENARIO;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.tools.ui.ComposableIdUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyComposableStepContent implements ChutneyContentProvider {

    private final ComposableStepRepository repository;
    private final ObjectMapper mapper;

    public ChutneyComposableStepContent(ComposableStepRepository repository,
                                        @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Override
    public String provider() {
        return "step_definitions";
    }

    @Override
    public ChutneyContentCategory category() {
        return SCENARIO;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.findAll().stream()
            .map(cs -> ComposableIdUtils.fromFrontId(cs.id))
            .map(repository::findById)
            .map(cs -> {
                ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName("[" + ComposableIdUtils.toFrontId(cs.id) + "]-" + cs.name);
                try {
                    builder
                        .withContent(mapper.writeValueAsString(componentType(cs)))
                        .withFormat("json");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return builder.build();
            });
    }

    private FSStepDefinition componentType(ComposableStep cs) {
        if (cs.implementation.isPresent()) {
            return new ActionStepDef(cs);
        }

        return new ComponentStepDef(cs);
    }

    private static class ActionStepDef implements FSStepDefinition {

        public final String id;
        public final String name;
        public final Map<String, String> executionParameters;
        public final Optional<String> implementation;
        public final Strategy strategy;
        public final List<String> tags;

        public ActionStepDef(ComposableStep cs) {
            this.id = cs.id;
            this.name = cs.name;
            this.executionParameters = cs.executionParameters;
            this.implementation = cs.implementation;
            this.strategy = cs.strategy;
            this.tags = cs.tags;
        }
    }

    private class ComponentStepDef implements FSStepDefinition {

        public final String id;
        public final String name;
        public final List<FSStepDefinition> steps;
        public final Strategy strategy;
        public final Map<String, String> executionParameters;
        public final List<String> tags;

        public ComponentStepDef(ComposableStep cs) {
            this.id = cs.id;
            this.name = cs.name;
            this.steps = cs.steps.stream()
                .map(SubComponentStepDef::new)
                .collect(toList());
            this.strategy = cs.strategy;
            this.executionParameters = cs.executionParameters;
            this.tags = cs.tags;
        }
    }

    private class SubComponentStepDef implements FSStepDefinition {

        public final String id;
        public final String name;
        public final Map<String, String> executionParameters;

        public SubComponentStepDef(ComposableStep cs) {
            this.id = cs.id;
            this.name = cs.name;
            this.executionParameters = cs.executionParameters;
        }
    }
}
