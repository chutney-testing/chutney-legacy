import { CampaignExecutionReport } from '.';

export class Campaign {

    constructor(public id?: number,
        public title: string = '',
        public description: string = '',
        public scenarioIds: Array<string> = [],
        public campaignExecutionReports: Array<CampaignExecutionReport> = [],
        public computedParameters: Map<string, string> = new Map(),
        public environment: string = '',
        public scheduleTime?: string,
        public parallelRun?: false,
        public retryAuto?: false,
        public datasetId?: string) {
    }
}
