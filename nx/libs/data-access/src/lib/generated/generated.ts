import { gql } from 'apollo-angular';
import { Injectable } from '@angular/core';
import * as Apollo from 'apollo-angular';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
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
  status: Scalars['String'];
  executions?: Maybe<Array<Maybe<ScenarioExecution>>>;
  tags?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type Query = {
  __typename?: 'Query';
  user?: Maybe<User>;
  scenarios?: Maybe<Array<Maybe<Scenario>>>;
};

export type Mutation = {
  __typename?: 'Mutation';
  login?: Maybe<User>;
  deleteScenario?: Maybe<Scalars['Boolean']>;
};


export type MutationLoginArgs = {
  input: LoginInput;
};


export type MutationDeleteScenarioArgs = {
  input: Scalars['ID'];
};



export type DeleteScenarioMutationVariables = Exact<{
  input: Scalars['ID'];
}>;


export type DeleteScenarioMutation = (
  { __typename?: 'Mutation' }
  & Pick<Mutation, 'deleteScenario'>
);

export type LoginMutationVariables = Exact<{
  input: LoginInput;
  bodySerializer: Scalars['RestFunctionOrString'];
}>;


export type LoginMutation = (
  { __typename?: 'Mutation' }
  & { login?: Maybe<(
    { __typename?: 'User' }
    & Pick<User, 'id' | 'name' | 'firstname' | 'lastname' | 'mail'>
  )> }
);

export type ScenariosQueryVariables = Exact<{ [key: string]: never; }>;


export type ScenariosQuery = (
  { __typename?: 'Query' }
  & { scenarios?: Maybe<Array<Maybe<(
    { __typename?: 'Scenario' }
    & Pick<Scenario, 'id' | 'title' | 'tags' | 'status'>
    & { executions?: Maybe<Array<Maybe<(
      { __typename: 'ScenarioExecution' }
      & Pick<ScenarioExecution, 'executionId' | 'time' | 'status' | 'duration'>
    )>>> }
  )>>> }
);

export type UserQueryVariables = Exact<{ [key: string]: never; }>;


export type UserQuery = (
  { __typename?: 'Query' }
  & { user?: Maybe<(
    { __typename?: 'User' }
    & Pick<User, 'id' | 'name' | 'firstname' | 'lastname' | 'mail'>
  )> }
);

export const DeleteScenarioDocument = gql`
    mutation deleteScenario($input: ID!) {
  deleteScenario(input: $input) @rest(type: "ScenarioDeleted", path: "api/scenario/v2/{args.input}", method: "DELETE")
}
    `;

  @Injectable({
    providedIn: 'root'
  })
  export class DeleteScenarioGQL extends Apollo.Mutation<DeleteScenarioMutation, DeleteScenarioMutationVariables> {
    document = DeleteScenarioDocument;
    
    constructor(apollo: Apollo.Apollo) {
      super(apollo);
    }
  }
export const LoginDocument = gql`
    mutation login($input: LoginInput!, $bodySerializer: RestFunctionOrString!) {
  login(input: $input) @rest(type: "User", path: "api/v1/user/login", method: "POST", bodySerializer: $bodySerializer) {
    id
    name
    firstname
    lastname
    mail
  }
}
    `;

  @Injectable({
    providedIn: 'root'
  })
  export class LoginGQL extends Apollo.Mutation<LoginMutation, LoginMutationVariables> {
    document = LoginDocument;
    
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
    executions {
      __typename
      executionId
      time
      status
      duration
    }
    status @client
  }
}
    `;

  @Injectable({
    providedIn: 'root'
  })
  export class ScenariosGQL extends Apollo.Query<ScenariosQuery, ScenariosQueryVariables> {
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
    providedIn: 'root'
  })
  export class UserGQL extends Apollo.Query<UserQuery, UserQueryVariables> {
    document = UserDocument;
    
    constructor(apollo: Apollo.Apollo) {
      super(apollo);
    }
  }