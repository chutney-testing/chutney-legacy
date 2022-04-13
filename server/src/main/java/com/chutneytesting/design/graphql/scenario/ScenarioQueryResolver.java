package com.chutneytesting.design.graphql.scenario;

import com.chutneytesting.design.api.scenario.v2_0.GwtTestCaseController;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.TestCaseIndexDto;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.kickstart.annotations.GraphQLQueryResolver;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@GraphQLQueryResolver
@GraphQLName("scenariosQueries")
public class ScenarioQueryResolver implements ApplicationContextAware {

    private static GwtTestCaseController testCaseController;


    @GraphQLField
    @GraphQLName("search")
    @GraphQLDescription("Search scenarios by keyword. Returns all if no keyword")
    public static List<TestCaseIndexDto> search(@Nullable @GraphQLName("keyword") String keyword) {
        return testCaseController.getTestCases(keyword);
    }

    @GraphQLField
    @GraphQLName("rawById")
    @GraphQLDescription("Raw scenario by id")
    public static RawTestCaseDto getRawById(@GraphQLNonNull @GraphQLName("id")String id) {
        return testCaseController.getTestCaseById(id);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        testCaseController = applicationContext.getBean(GwtTestCaseController.class);
    }
}
