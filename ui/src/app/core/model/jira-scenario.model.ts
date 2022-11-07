export class JiraScenario {
    constructor(
        public id: string,
        public chutneyId: string,
        public executionStatus?: string) {
    }
}

export class JiraTestExecutionScenarios {
    constructor(
        public id: string,
        public jiraScenarios: JiraScenario[]) {
    }
}

export enum XrayStatus {
    PASS = 'PASS',
    FAIL = 'FAIL'
}
