import { ScenarioExecutionReportOutline } from '.';

export class CampaignExecutionReport {
    constructor(
        public executionId?: number,
        public scenarioExecutionReports: Array<ScenarioExecutionReportOutline> = [],
        public status?: string,
        public duration?: string,
        public startDate?: string,
        public campaignName?: string,
        public partialExecution?: boolean,
        public executionEnvironment?: string) { }
}
