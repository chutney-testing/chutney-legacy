package com.chutneytesting.design.api.scenario.v2_0.dto;

import graphql.annotations.annotationTypes.GraphQLConstructor;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;



public class RawTestCaseRequestDto {

    @GraphQLField
    @GraphQLName("scenario")
    @GraphQLNonNull
    private String scenario;

    @GraphQLField
    @GraphQLName("id")
    private String id;

    @GraphQLField
    @GraphQLName("title")
    @GraphQLNonNull
    private String title;

    @GraphQLField
    @GraphQLName("description")
    @GraphQLNonNull
    private String description;

    @GraphQLField
    @GraphQLName("tags")
    private List<String> tags;

    public RawTestCaseRequestDto() {
    }

    @GraphQLConstructor
    public RawTestCaseRequestDto(String scenario, String id, String title, String description, List<String> tags) {
        this.scenario = scenario;
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
    }

    public String getScenario() {
        return scenario;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }
}
