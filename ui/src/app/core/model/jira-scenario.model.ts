export class JiraScenario {
    constructor(
        public id: string,
        public chutneyId: string,
        public executionStatus?: string) {
    }
}

export enum XrayStatus {
    PASS = 'PASS',
    FAIL = 'FAIL'
}
