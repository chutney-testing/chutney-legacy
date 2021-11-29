import { ScenarioType } from '@model';

export class ChutneyState {
  constructor(
    public tags: Array<String> = [],
    public campaignTags: Array<String> = [],
    public scenarioTypes: Array<ScenarioType> = [],
    public noTag: boolean,
    public campaignNoTag: boolean
  ) { }
}
