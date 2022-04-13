package com.chutneytesting;

import graphql.Scalars;
import graphql.kickstart.tools.TypeDefinitionFactory;
import graphql.language.Definition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphqlConfiguration {

  /*  class MyTypeDefinitionFactory implements TypeDefinitionFactory {
        public List<Definition<?>> create(final List<Definition<?>> existing) {
            return ObjectTypeDefinition.newObjectTypeDefinition()
                .name("MyType")
                .fieldDefinition(new fieldDefinition("myField", new TypeName("String")))
                .build();
        }
    }*/
}
