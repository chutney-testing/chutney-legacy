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

export type ScenarioInput = {
  id?: Maybe<Scalars['ID']>;
  title: Scalars['String'];
  description: Scalars['String'];
  tags?: Maybe<Array<Scalars['String']>>;
  content?: Maybe<Scalars['String']>;
};

export type CampaignInput = {
  id?: Maybe<Scalars['ID']>;
  title: Scalars['String'];
  description: Scalars['String'];
  environment?: Maybe<Scalars['String']>;
  scenarioIds?: Maybe<Array<Scalars['String']>>;
};

export type ScenariosFilter = {
  __typename?: 'ScenariosFilter';
  text?: Maybe<Scalars['String']>;
  tags?: Maybe<Array<Scalars['String']>>;
  date?: Maybe<Scalars['String']>;
  advanced?: Maybe<Scalars['Boolean']>;
};

export type Campaign = {
  __typename?: 'Campaign';
  id: Scalars['ID'];
  title: Scalars['String'];
  description: Scalars['String'];
  scenarios?: Maybe<Array<Maybe<Scenario>>>;
};

export type CampaignExecution = {
  __typename?: 'CampaignExecution';
  campaignName: Scalars['String'];
  executionId: Scalars['ID'];
  duration: Scalars['Int'];
  status?: Maybe<Scalars['String']>;
  executionEnvironment: Scalars['String'];
};

export type CampaignExecutionReport = {
  __typename?: 'CampaignExecutionReport';
  campaignName: Scalars['String'];
  executionEnvironment: Scalars['String'];
  duration: Scalars['Int'];
  executionId: Scalars['ID'];
  status?: Maybe<Scalars['String']>;
  startDate?: Maybe<Scalars['String']>;
  scenarioExecutionReports?: Maybe<Array<Maybe<ScenarioExecutionReport>>>;
};

export type ScenarioExecutionReport = {
  __typename?: 'ScenarioExecutionReport';
  duration: Scalars['Int'];
  error?: Maybe<Scalars['String']>;
  executionId: Scalars['Int'];
  info?: Maybe<Scalars['String']>;
  scenarioId: Scalars['Int'];
  scenarioName?: Maybe<Scalars['String']>;
  startDate?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
};

export type Query = {
  __typename?: 'Query';
  user?: Maybe<User>;
  scenarios?: Maybe<Array<Maybe<Scenario>>>;
  campaigns?: Maybe<Array<Maybe<Campaign>>>;
  campaign?: Maybe<Campaign>;
  scenariosFilter?: Maybe<ScenariosFilter>;
  scenario?: Maybe<Scenario>;
  runScenarioHistory: ScenarioExecution;
  campaignExecutionReport: CampaignExecutionReport;
};

export type QueryCampaignArgs = {
  campaignId: Scalars['ID'];
};

export type QueryScenarioArgs = {
  scenarioId: Scalars['ID'];
};

export type QueryRunScenarioHistoryArgs = {
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type QueryCampaignExecutionReportArgs = {
  campaignId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type Mutation = {
  __typename?: 'Mutation';
  login?: Maybe<User>;
  saveCampaign?: Maybe<Campaign>;
  updateCampaign?: Maybe<Campaign>;
  saveScenario?: Maybe<Scalars['Boolean']>;
  deleteScenario?: Maybe<Scalars['Boolean']>;
  runScenario: Scalars['ID'];
  pauseScenario?: Maybe<Scalars['Boolean']>;
  resumeScenario?: Maybe<Scalars['Boolean']>;
  stopScenario?: Maybe<Scalars['Boolean']>;
  stopCampaign?: Maybe<Scalars['Boolean']>;
  runCampaign: CampaignExecution;
  deleteCampaign?: Maybe<Scalars['Boolean']>;
};

export type MutationLoginArgs = {
  input: LoginInput;
};

export type MutationSaveCampaignArgs = {
  input: CampaignInput;
};

export type MutationUpdateCampaignArgs = {
  input: CampaignInput;
};

export type MutationSaveScenarioArgs = {
  input: ScenarioInput;
};

export type MutationDeleteScenarioArgs = {
  input: Scalars['ID'];
};

export type MutationRunScenarioArgs = {
  scenarioId: Scalars['ID'];
  environment?: Maybe<Scalars['String']>;
  dataset?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type MutationPauseScenarioArgs = {
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type MutationResumeScenarioArgs = {
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type MutationStopScenarioArgs = {
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type MutationStopCampaignArgs = {
  campaignId: Scalars['ID'];
  executionId: Scalars['ID'];
};

export type MutationRunCampaignArgs = {
  campaignId: Scalars['ID'];
  environment?: Maybe<Scalars['String']>;
};

export type MutationDeleteCampaignArgs = {
  input: Scalars['ID'];
};

export type CampaignExecutionReportQueryVariables = Exact<{
  campaignId: Scalars['ID'];
  executionId: Scalars['ID'];
}>;

export type CampaignExecutionReportQuery = { __typename?: 'Query' } & {
  campaignExecutionReport: { __typename?: 'CampaignExecutionReport' } & Pick<
    CampaignExecutionReport,
    | 'campaignName'
    | 'executionEnvironment'
    | 'duration'
    | 'executionId'
    | 'status'
    | 'startDate'
  > & {
      scenarioExecutionReports?: Maybe<
        Array<
          Maybe<
            { __typename?: 'ScenarioExecutionReport' } & Pick<
              ScenarioExecutionReport,
              | 'duration'
              | 'error'
              | 'executionId'
              | 'info'
              | 'scenarioId'
              | 'scenarioName'
              | 'startDate'
              | 'status'
            >
          >
        >
      >;
    };
};

export type CampaignQueryVariables = Exact<{
  campaignId: Scalars['ID'];
}>;

export type CampaignQuery = { __typename?: 'Query' } & {
  campaign?: Maybe<
    { __typename?: 'Campaign' } & Pick<
      Campaign,
      'id' | 'title' | 'description'
    > & {
        scenarios?: Maybe<
          Array<
            Maybe<
              { __typename?: 'Scenario' } & Pick<
                Scenario,
                'id' | 'title' | 'description' | 'status'
              >
            >
          >
        >;
      }
  >;
};

export type CampaignsQueryVariables = Exact<{ [key: string]: never }>;

export type CampaignsQuery = { __typename?: 'Query' } & {
  campaigns?: Maybe<
    Array<
      Maybe<
        { __typename?: 'Campaign' } & Pick<
          Campaign,
          'id' | 'title' | 'description'
        >
      >
    >
  >;
};

export type DeleteCampaignMutationVariables = Exact<{
  input: Scalars['ID'];
}>;

export type DeleteCampaignMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'deleteCampaign'
>;

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

export type PauseScenarioMutationVariables = Exact<{
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
  bodyBuilder: Scalars['RestFunction'];
}>;

export type PauseScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'pauseScenario'
>;

export type ResumeScenarioMutationVariables = Exact<{
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
  bodyBuilder: Scalars['RestFunction'];
}>;

export type ResumeScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'resumeScenario'
>;

export type RunCampaignMutationVariables = Exact<{
  campaignId: Scalars['ID'];
  environment: Scalars['String'];
}>;

export type RunCampaignMutation = { __typename?: 'Mutation' } & {
  runCampaign: { __typename?: 'CampaignExecution' } & Pick<
    CampaignExecution,
    'campaignName' | 'executionId' | 'duration' | 'status'
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
  environment: Scalars['String'];
  dataset?: Maybe<Array<Maybe<Scalars['String']>>>;
}>;

export type RunScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'runScenario'
>;

export type SaveCampaignMutationVariables = Exact<{
  input: CampaignInput;
}>;

export type SaveCampaignMutation = { __typename?: 'Mutation' } & {
  saveCampaign?: Maybe<
    { __typename?: 'Campaign' } & Pick<Campaign, 'description' | 'id' | 'title'>
  >;
};

export type SaveScenarioMutationVariables = Exact<{
  input: ScenarioInput;
}>;

export type SaveScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'saveScenario'
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
          | 'id'
          | 'title'
          | 'description'
          | 'tags'
          | 'creationDate'
          | 'executionDate'
          | 'status'
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

export type StopCampaignMutationVariables = Exact<{
  campaignId: Scalars['ID'];
  executionId: Scalars['ID'];
  bodyBuilder: Scalars['RestFunction'];
}>;

export type StopCampaignMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'stopCampaign'
>;

export type StopScenarioMutationVariables = Exact<{
  scenarioId: Scalars['ID'];
  executionId: Scalars['ID'];
  bodyBuilder: Scalars['RestFunction'];
}>;

export type StopScenarioMutation = { __typename?: 'Mutation' } & Pick<
  Mutation,
  'stopScenario'
>;

export type UpdateCampaignMutationVariables = Exact<{
  input: CampaignInput;
}>;

export type UpdateCampaignMutation = { __typename?: 'Mutation' } & {
  updateCampaign?: Maybe<
    { __typename?: 'Campaign' } & Pick<Campaign, 'description' | 'id' | 'title'>
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

export const CampaignExecutionReportDocument = gql`
  query campaignExecutionReport($campaignId: ID!, $executionId: ID!) {
    campaignExecutionReport(campaignId: $campaignId, executionId: $executionId)
      @rest(
        type: "CampaignExecutionReport"
        path: "api/ui/campaign/v1/{args.campaignId}/execution/{args.executionId}"
      ) {
      campaignName
      executionEnvironment
      duration
      executionId
      status
      startDate
      scenarioExecutionReports {
        duration
        error
        executionId
        info
        scenarioId
        scenarioName
        startDate
        status
      }
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class CampaignExecutionReportGQL extends Apollo.Query<
  CampaignExecutionReportQuery,
  CampaignExecutionReportQueryVariables
> {
  document = CampaignExecutionReportDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const CampaignDocument = gql`
  query campaign($campaignId: ID!) {
    campaign(campaignId: $campaignId)
      @rest(type: "Campaign", path: "api/ui/campaign/v1/{args.campaignId}") {
      id @export(as: "id")
      title
      description
      scenarios
        @rest(
          type: "Scenario"
          path: "api/ui/campaign/v1/{exportVariables.id}/scenarios"
        ) {
        id
        title
        description
        status
      }
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class CampaignGQL extends Apollo.Query<
  CampaignQuery,
  CampaignQueryVariables
> {
  document = CampaignDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const CampaignsDocument = gql`
  query campaigns {
    campaigns @rest(type: "Campaign", path: "api/ui/campaign/v1") {
      id
      title
      description
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class CampaignsGQL extends Apollo.Query<
  CampaignsQuery,
  CampaignsQueryVariables
> {
  document = CampaignsDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const DeleteCampaignDocument = gql`
  mutation deleteCampaign($input: ID!) {
    deleteCampaign(input: $input)
      @rest(
        type: "CampaignDeleted"
        path: "api/ui/campaign/v1/{args.input}"
        method: "DELETE"
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class DeleteCampaignGQL extends Apollo.Mutation<
  DeleteCampaignMutation,
  DeleteCampaignMutationVariables
> {
  document = DeleteCampaignDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
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
export const PauseScenarioDocument = gql`
  mutation pauseScenario(
    $scenarioId: ID!
    $executionId: ID!
    $bodyBuilder: RestFunction!
  ) {
    pauseScenario(scenarioId: $scenarioId, executionId: $executionId)
      @rest(
        type: "PauseScenarioExecution"
        path: "api/ui/scenario/executionasync/v1/{args.scenarioId}/execution/{args.executionId}/pause"
        method: "POST"
        bodyBuilder: $bodyBuilder
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class PauseScenarioGQL extends Apollo.Mutation<
  PauseScenarioMutation,
  PauseScenarioMutationVariables
> {
  document = PauseScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const ResumeScenarioDocument = gql`
  mutation resumeScenario(
    $scenarioId: ID!
    $executionId: ID!
    $bodyBuilder: RestFunction!
  ) {
    resumeScenario(scenarioId: $scenarioId, executionId: $executionId)
      @rest(
        type: "ResumeScenarioExecution"
        path: "api/ui/scenario/executionasync/v1/{args.scenarioId}/execution/{args.executionId}/resume"
        method: "POST"
        bodyBuilder: $bodyBuilder
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class ResumeScenarioGQL extends Apollo.Mutation<
  ResumeScenarioMutation,
  ResumeScenarioMutationVariables
> {
  document = ResumeScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const RunCampaignDocument = gql`
  mutation runCampaign($campaignId: ID!, $environment: String!) {
    runCampaign(campaignId: $campaignId, environment: $environment)
      @rest(
        type: "CampaignExecution"
        path: "api/ui/campaign/execution/v1/byID/{args.campaignId}/{args.environment}"
        method: "GET"
      ) {
      campaignName
      executionId
      duration
      status
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class RunCampaignGQL extends Apollo.Mutation<
  RunCampaignMutation,
  RunCampaignMutationVariables
> {
  document = RunCampaignDocument;

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
  mutation runScenario(
    $scenarioId: ID!
    $environment: String!
    $dataset: [String]
  ) {
    runScenario(
      scenarioId: $scenarioId
      environment: $environment
      dataset: $dataset
    )
      @rest(
        type: "SceanrioExecution"
        path: "api/ui/scenario/executionasync/v1/{args.scenarioId}/{args.environment}"
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
export const SaveCampaignDocument = gql`
  mutation saveCampaign($input: CampaignInput!) {
    saveCampaign(input: $input)
      @rest(type: "Campaign", path: "api/ui/campaign/v1", method: "POST") {
      description
      id
      title
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class SaveCampaignGQL extends Apollo.Mutation<
  SaveCampaignMutation,
  SaveCampaignMutationVariables
> {
  document = SaveCampaignDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const SaveScenarioDocument = gql`
  mutation saveScenario($input: ScenarioInput!) {
    saveScenario(input: $input)
      @rest(type: "Scenario", path: "api/scenario/v2/raw", method: "POST")
  }
`;

@Injectable({
  providedIn: 'root',
})
export class SaveScenarioGQL extends Apollo.Mutation<
  SaveScenarioMutation,
  SaveScenarioMutationVariables
> {
  document = SaveScenarioDocument;

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
      description
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
export const StopCampaignDocument = gql`
  mutation stopCampaign(
    $campaignId: ID!
    $executionId: ID!
    $bodyBuilder: RestFunction!
  ) {
    stopCampaign(campaignId: $campaignId, executionId: $executionId)
      @rest(
        type: "StopCampaignExecution"
        path: "api/ui/campaign/execution/v1/{args.executionId}/stop"
        method: "POST"
        bodyBuilder: $bodyBuilder
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class StopCampaignGQL extends Apollo.Mutation<
  StopCampaignMutation,
  StopCampaignMutationVariables
> {
  document = StopCampaignDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const StopScenarioDocument = gql`
  mutation stopScenario(
    $scenarioId: ID!
    $executionId: ID!
    $bodyBuilder: RestFunction!
  ) {
    stopScenario(scenarioId: $scenarioId, executionId: $executionId)
      @rest(
        type: "StopScenarioExecution"
        path: "api/ui/scenario/executionasync/v1/{args.scenarioId}/execution/{args.executionId}/stop"
        method: "POST"
        bodyBuilder: $bodyBuilder
      )
  }
`;

@Injectable({
  providedIn: 'root',
})
export class StopScenarioGQL extends Apollo.Mutation<
  StopScenarioMutation,
  StopScenarioMutationVariables
> {
  document = StopScenarioDocument;

  constructor(apollo: Apollo.Apollo) {
    super(apollo);
  }
}
export const UpdateCampaignDocument = gql`
  mutation updateCampaign($input: CampaignInput!) {
    updateCampaign(input: $input)
      @rest(type: "Campaign", path: "api/ui/campaign/v1", method: "PUT") {
      description
      id
      title
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class UpdateCampaignGQL extends Apollo.Mutation<
  UpdateCampaignMutation,
  UpdateCampaignMutationVariables
> {
  document = UpdateCampaignDocument;

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
