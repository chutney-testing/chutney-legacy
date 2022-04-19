package com.chutneytesting.design.graphql.scenario;

import com.chutneytesting.design.api.scenario.v2_0.GwtTestCaseController;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseRequestDto;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.kickstart.annotations.GraphQLMutationResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
@GraphQLMutationResolver
@GraphQLName("scenariosMutations")
public class ScenarioMutationResolver implements ApplicationContextAware {

    private static GwtTestCaseController testCaseController;

    @GraphQLField
    @GraphQLNonNull
    @GraphQLName("save")
    @GraphQLDescription("Save raw scenario")
    public static String save(final @GraphQLNonNull @GraphQLName("scenario") RawTestCaseRequestDto rawTestCaseDto) {
        return testCaseController.saveTestCase(ImmutableRawTestCaseDto.builder()
            .description(rawTestCaseDto.getDescription())
            .title(rawTestCaseDto.getTitle())
            .tags(rawTestCaseDto.getTags())
            .scenario(rawTestCaseDto.getScenario())
            .build());
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        testCaseController = applicationContext.getBean(GwtTestCaseController.class);
    }
}
