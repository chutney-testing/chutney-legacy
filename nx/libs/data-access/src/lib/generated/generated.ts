import { gql } from 'apollo-angular';
import { Injectable } from '@angular/core';
import * as Apollo from 'apollo-angular';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = {
  [K in keyof T]: T[K];
};
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  RestFunction: any;
  RestFunctionOrString: any;
};

export type LoginInput = {
  username: Scalars['String'];
  password: Scalars['String'];
};

export type User = {
  __typename?: 'User';
  id: Scalars['ID'];
  name: Scalars['String'];
  firstname: Scalars['String'];
  lastname: Scalars['String'];
  mail: Scalars['String'];
};

export type ScenarioExecution = {
  __typename?: 'ScenarioExecution';
  executionId: Scalars['ID'];
  time: Scalars['String'];
  duration: Scalars['Int'];
  status?: Maybe<Scalars['String']>;
  info?: Maybe<Scalars['String']>;
  error?: Maybe<Scalars['String']>;
  testCaseTitle: Scalars['String'];
  environment: Scalars['String'];
  report?: Maybe<Scalars['String']>;
};

export type Scenario = {
  __typename?: 'Scenario';
  id: Scalars['ID'];
  title: Scalars['String'];
  description: Scalars['String'];
  status: Scalars['String'];
  executions?: Maybe<Array<Maybe<ScenarioExecution>>>;
  tags?: Maybe<Array<Maybe<Scalars['String']>>>;
  creationDate?: Maybe<Scalars['String']>;
  executionDate?: Maybe<Scalars['String']>;
  content?: Maybe<Scalars['String']>;
};

export type ScenariosFilter = {
  __typename?: 'ScenariosFilter';
  text?: Maybe<Scalars['String']>;
  tags?: Maybe<Array<Scalars['String']>>;
  date?: Maybe<Scalars['String']>;
  advanced?: Maybe<Scalars['Boolean']>;
};

export type Query = {
  __typename?: 'Query';
  user?: Maybe<User>;
  scenarios?: Maybe<Array<Maybe<Scenario>>>;
  scenariosFilter?: Maybe<ScenariosFilter>;
  scenario?: Maybe<Scenario>;
  runScenarioHistory: ScenarioExecution;
};

export type QueryScenarioArgs = {
  scenarioId: Scalars['ID'];
};

export type QueryRunScenarioHistoryArgs = {
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type Mutation = {
  __typename?: 'Mutation';
  login?: Maybe<User>;
  deleteScenario?: Maybe<Scalars['Boolean']>;
  runScenario: Scalars['ID'];
};

export type MutationLoginArgs = {
  input: LoginInput;
};

export type MutationDeleteScenarioArgs = {
  input: Scalars['ID'];
};

export type MutationRunScenarioArgs = {
  scenarioId: Scalars['ID'];
  dataset?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type DeleteScenarioMutationVariables = Exact<{
  input: Scalars['ID'];
}>;

export type DeleteScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'deleteScenario'
>;

export type LoginMutationVariables = Exact<{
  input: LoginInput;
  bodySerializer: Scalars['RestFunctionOrString'];
}>;

export type LoginMutation = { __typename?: 'Mutation' } & {
  login?: Maybe<
    { __typename?: 'User' } & Pick<
      User,
      'id' | 'name' | 'firstname' | 'lastname' | 'mail'
    >
  >;
};

export type RunScenarioHistoryQueryVariables = Exact<{
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
}>;

export type RunScenarioHistoryQuery = { __typename?: 'Query' } & {
  runScenarioHistory: { __typename?: 'ScenarioExecution' } & Pick<
    ScenarioExecution,
    | 'executionId'
    | 'time'
    | 'duration'
    | 'status'
    | 'info'
    | 'error'
    | 'testCaseTitle'
    | 'environment'
    | 'report'
  >;
};

export type RunScenarioMutationVariables = Exact<{
  scenarioId: Scalars['ID'];
  dataset?: Maybe<Array<Maybe<Scalars['String']>>>;
}>;

export type RunScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'runScenario'
>;

export type ScenarioQueryVariables = Exact<{
  scenarioId: Scalars['ID'];
}>;

export type ScenarioQuery = { __typename?: 'Query' } & {
  scenario?: Maybe<
    { __typename?: 'Scenario' } & Pick<
      Scenario,
      | 'id'
      | 'title'
      | 'description'
      | 'tags'
      | 'status'
      | 'creationDate'
      | 'executionDate'
      | 'content'
    >
  >;
};

export type ScenariosFilterQueryVariables = Exact<{ [key: string]: never }>;

export type ScenariosFilterQuery = { __typename?: 'Query' } & {
  scenariosFilter?: Maybe<
    { __typename?: 'ScenariosFilter' } & Pick<
      ScenariosFilter,
      'text' | 'date' | 'tags' | 'advanced'
    >
  >;
};

export type ScenariosQueryVariables = Exact<{ [key: string]: never }>;

export type ScenariosQuery = { __typename?: 'Query' } & {
  scenarios?: Maybe<
    Array<
      Maybe<
        { __typename?: 'Scenario' } & Pick<
          Scenario,
          'id' | 'title' | 'tags' | 'creationDate' | 'executionDate' | 'status'
        > & {
            executions?: Maybe<
              Array<
                Maybe<
                  { __typename: 'ScenarioExecution' } & Pick<
                    ScenarioExecution,
                    'executionId' | 'time' | 'status' | 'duration'
                  >
                >
              >
            >;
          }
      >
    >
  >;
};

export type UserQueryVariables = Exact<{ [key: string]: never }>;

export type UserQuery = { __typename?: 'Query' } & {
  user?: Maybe<
    { __typename?: 'User' } & Pick<
      User,
      'id' | 'name' | 'firstname' | 'lastname' | 'mail'
    >
  >;
};

export const DeleteScenarioDocument = gql`
  mutation deleteScenario($input: ID!) {
    deleteScenario(input: $input)
      @rest(
        type: "ScenarioDeleted"
        path: "api/scenario/v2/{args.input}"
        method: "DELETE"
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class DeleteScenarioGQL extends Apollo.Mutation<
  DeleteScenarioMutation,
  DeleteScenarioMutationVariables
> {
  document = DeleteScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const LoginDocument = gql`
  mutation login($input: LoginInput!, $bodySerializer: RestFunctionOrString!) {
    login(input: $input)
      @rest(
        type: "User"
        path: "api/v1/user/login"
        method: "POST"
        bodySerializer: $bodySerializer
      ) {
      id
      name
      firstname
      lastname
      mail
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class LoginGQL extends Apollo.Mutation<
  LoginMutation,
  LoginMutationVariables
> {
  document = LoginDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const RunScenarioHistoryDocument = gql`
  query runScenarioHistory($scenarioId: ID!, $executionId: ID!) {
    runScenarioHistory(scenarioId: $scenarioId, executionId: $executionId)
      @rest(
        type: "ScenarioExecution"
        path: "api/ui/scenario/{args.scenarioId}/execution/{args.executionId}/v1"
      ) {
      executionId
      time
      duration
      status
      info
      error
      testCaseTitle
      environment
      report
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class RunScenarioHistoryGQL extends Apollo.Query<
  RunScenarioHistoryQuery,
  RunScenarioHistoryQueryVariables
> {
  document = RunScenarioHistoryDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const RunScenarioDocument = gql`
  mutation runScenario($scenarioId: ID!, $dataset: [String]) {
    runScenario(scenarioId: $scenarioId, dataset: $dataset)
      @rest(
        type: "SceanrioExecution"
        path: "api/ui/scenario/executionasync/v1/{args.scenarioId}/GLOBAL"
        method: "POST"
        bodyKey: "dataset"
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class RunScenarioGQL extends Apollo.Mutation<
  RunScenarioMutation,
  RunScenarioMutationVariables
> {
  document = RunScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const ScenarioDocument = gql`
  query scenario($scenarioId: ID!) {
    scenario(scenarioId: $scenarioId)
      @rest(type: "Scenario", path: "api/scenario/v2/raw/{args.scenarioId}") {
      id
      title
      description
      tags
      status
      creationDate
      executionDate
      content
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class ScenarioGQL extends Apollo.Query<
  ScenarioQuery,
  ScenarioQueryVariables
> {
  document = ScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const ScenariosFilterDocument = gql`
  query scenariosFilter {
    scenariosFilter @client {
      text
      date
      tags
      advanced
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class ScenariosFilterGQL extends Apollo.Query<
  ScenariosFilterQuery,
  ScenariosFilterQueryVariables
> {
  document = ScenariosFilterDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const ScenariosDocument = gql`
  query scenarios {
    scenarios @rest(type: "Scenario", path: "api/scenario/v2") {
      id
      title
      tags
      executions @type(name: "ScenarioExecution") {
        __typename
        executionId
        time
        status
        duration
      }
      creationDate
      executionDate
      status @client
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class ScenariosGQL extends Apollo.Query<
  ScenariosQuery,
  ScenariosQueryVariables
> {
  document = ScenariosDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const UserDocument = gql`
  query user {
    user @client {
      id
      name
      firstname
      lastname
      mail
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class UserGQL extends Apollo.Query<UserQuery, UserQueryVariables> {
  document = UserDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
