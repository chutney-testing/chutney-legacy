export class StepExecutionReport {
    constructor(
        public duration: number,
        public status: string,
        public startDate: string,
        public information: string[],
        public errors: string[],
        public type: string,
        public strategy: string,
        public targetName: string,
        public targetUrl: string,
        public evaluatedInputs: Map<string, Object>,
        public steps: Array<StepExecutionReport>,
        public name?: string) {}
}
