import { ScenarioType } from '@model';

export class ChutneyState {
  constructor(
    public tags: Array<String> = [],
    public scenarioList,
    public scenarioTypes: Array<ScenarioType> = [],
    public noTag: boolean
  ) { }
}
