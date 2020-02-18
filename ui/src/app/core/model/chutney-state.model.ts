import { ScenarioType } from '@model';

export class ChutneyState {
  constructor(
    public tags: Array<String> = [],
    public scenarioList: boolean = false,
    public scenarioTypes: Array<ScenarioType> = []
  ) { }
}
