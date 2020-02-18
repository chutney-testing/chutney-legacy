export class ScenarioExecutionReportOutline {
    constructor(
        public scenarioId?: string,
        public executionId?: number,
        public duration?: number,
        public scenarioName?: string,
        public status?: string,
        public startDate?: Date,
        public info: Array<string> = [],
        public error: Array<string> = []
    ) {}
}
