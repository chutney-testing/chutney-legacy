import { InMemoryCache } from '@apollo/client/core';
import { NgModule } from '@angular/core';

import { RestLink } from 'apollo-link-rest';
import { APOLLO_OPTIONS } from 'apollo-angular';
import { sortByKeys } from '@chutney/utils';
import { ScenarioExecution } from '@chutney/data-access';

const inMemoryCache = new InMemoryCache();

const restLink = new RestLink({
  uri: window.location.origin + '/',
  typePatcher: {
    Scenario: (
      data: any,
      outerType: string,
      patchDeeper: RestLink.FunctionalTypePatcher
    ): any => {
      if (data.metadata) {
        data = { __typename: 'Scenario', ...data.metadata };
      }
      return data;
    },
    /* … other nested type patchers … */
  },
});

const resolvers = {
  Query: {
    user: (_, variables, { cache }) => {
      const item = localStorage.getItem('user');
      if (item) {
        return JSON.parse(item);
      }
      return null;
    },
  },
  Mutation: {},
  Scenario: {
    status: (data: any, variables: any, { _cache }) =>
      sortByKeys<ScenarioExecution>(data.executions || [], '-time')[0]
        ?.status || 'NOT_EXECUTED',
  },
};

const defaultOptions = {
  watchQuery: {
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'ignore',
  },
  query: {
    fetchPolicy: 'network-only',
    errorPolicy: 'all',
  },
  mutate: {
    errorPolicy: 'all',
  },
};

export function createApollo() {
  return {
    link: restLink,
    cache: inMemoryCache,
    resolvers,
    defaultOptions,
  };
}

@NgModule({
  providers: [
    {
      provide: APOLLO_OPTIONS,
      useFactory: createApollo,
    },
  ],
})
export class GraphQLModule {}
