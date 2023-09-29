
export class User {
  constructor(
    public id: string,
    public name?: string,
    public firstname?: string,
    public lastname?: string,
    public mail?: string,
    public authorizations?: Array<Authorization>,
  ) { }
}

export enum Authorization {
    SCENARIO_READ = 'SCENARIO_READ',
    SCENARIO_WRITE = 'SCENARIO_WRITE',
    SCENARIO_EXECUTE = 'SCENARIO_EXECUTE',

    CAMPAIGN_READ = 'CAMPAIGN_READ',
    CAMPAIGN_WRITE = 'CAMPAIGN_WRITE',
    CAMPAIGN_EXECUTE = 'CAMPAIGN_EXECUTE',

    ENVIRONMENT_ACCESS = 'ENVIRONMENT_ACCESS',

    GLOBAL_VAR_READ = 'GLOBAL_VAR_READ',
    GLOBAL_VAR_WRITE = 'GLOBAL_VAR_WRITE',

    DATASET_READ = 'DATASET_READ',
    DATASET_WRITE = 'DATASET_WRITE',

    ADMIN_ACCESS = 'ADMIN_ACCESS'
}
