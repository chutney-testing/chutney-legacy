import { StepExecutionReport } from './step-execution-report.model';

export class ScenarioExecutionReport {
    constructor(
        public executionId: number,
        public report: StepExecutionReport,
        public environment: string,
        public user: string,
        public scenarioName?: string
    ) { }
}
